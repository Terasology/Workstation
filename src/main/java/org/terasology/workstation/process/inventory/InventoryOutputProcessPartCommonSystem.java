/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.workstation.process.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.processPart.ProcessEntityFinishExecutionEvent;
import org.terasology.workstation.processPart.ProcessEntityIsInvalidEvent;
import org.terasology.workstation.processPart.ProcessEntityIsInvalidToStartEvent;
import org.terasology.workstation.processPart.inventory.ProcessEntityIsInvalidForInventoryItemEvent;
import org.terasology.workstation.processPart.metadata.ProcessEntityGetOutputDescriptionEvent;
import org.terasology.workstation.system.WorkstationRegistry;

import java.util.Set;

@RegisterSystem
public class InventoryOutputProcessPartCommonSystem extends BaseComponentSystem {
    public static final String WORKSTATIONOUTPUTCATEGORY = "OUTPUT";
    private static final Logger logger = LoggerFactory.getLogger(InventoryOutputProcessPartCommonSystem.class);

    @In
    InventoryManager inventoryManager;
    @In
    WorkstationRegistry workstationRegistry;
    @In
    BlockManager blockManager;
    @In
    EntityManager entityManager;

    @ReceiveEvent
    public void validateProcess(ProcessEntityIsInvalidEvent event, EntityRef processEntity,
                                InventoryOutputComponent inventoryOutputComponent) {

        Set<EntityRef> items = InventoryProcessPartUtils.createItems(inventoryOutputComponent.blockCounts, inventoryOutputComponent.itemCounts, false, entityManager, blockManager);
        try {
            if (items.size() == 0) {
                event.addError("No output items specified in " + this.getClass().getSimpleName());
            }
        } catch (Exception ex) {
            event.addError("Could not create output items in " + this.getClass().getSimpleName());
        } finally {
            if (items != null) {
                for (EntityRef outputItem : items) {
                    outputItem.destroy();
                }
            }
        }
    }

    @Priority(EventPriority.PRIORITY_LOW)
    @ReceiveEvent
    public void validateToStartExecution(ProcessEntityIsInvalidToStartEvent event, EntityRef processEntity,
                                         InventoryOutputComponent inventoryOutputComponent) {
        Set<EntityRef> outputItems = InventoryProcessPartUtils.createItems(inventoryOutputComponent.blockCounts, inventoryOutputComponent.itemCounts, false, entityManager, blockManager);
        // allow other systems to post process these items
        processEntity.addComponent(new InventoryOutputItemsComponent(outputItems));
        if (!InventoryProcessPartUtils.canGiveItemsTo(event.getWorkstation(), outputItems, WORKSTATIONOUTPUTCATEGORY)) {
            event.consume();
        }
        processEntity.removeComponent(InventoryOutputItemsComponent.class);
    }

    @Priority(EventPriority.PRIORITY_LOW)
    @ReceiveEvent
    public void finish(ProcessEntityFinishExecutionEvent event, EntityRef processEntity,
                       InventoryOutputComponent inventoryOutputComponent) {
        Set<EntityRef> outputItems = InventoryProcessPartUtils.createItems(inventoryOutputComponent.blockCounts, inventoryOutputComponent.itemCounts, true, entityManager, blockManager);
        // allow other systems to post process these items
        processEntity.addComponent(new InventoryOutputItemsComponent(outputItems));
        for (EntityRef outputItem : outputItems) {
            if (!inventoryManager.giveItem(event.getWorkstation(), event.getInstigator(), outputItem, WorkstationInventoryUtils.getAssignedOutputSlots(event.getWorkstation(), WORKSTATIONOUTPUTCATEGORY))) {
                outputItem.destroy();
            }
        }
    }

    @ReceiveEvent
    public void isValidInventoryItem(ProcessEntityIsInvalidForInventoryItemEvent event, EntityRef processEntity,
                                     InventoryOutputComponent inventoryOutputComponent) {
        // only allow the workstation to put items in the output
        if (WorkstationInventoryUtils.getAssignedOutputSlots(event.getWorkstation(), WORKSTATIONOUTPUTCATEGORY).contains(event.getSlotNo())
                && !event.getInstigator().equals(event.getWorkstation())) {
            event.consume();
        }
    }

    @ReceiveEvent
    public void getOutputDescriptions(ProcessEntityGetOutputDescriptionEvent event, EntityRef processEntity,
                                      InventoryOutputComponent inventoryOutputComponent) {
        Set<EntityRef> items = InventoryProcessPartUtils.createItems(inventoryOutputComponent.blockCounts, inventoryOutputComponent.itemCounts, false, entityManager, blockManager);
        try {
            for (EntityRef item : items) {
                event.addOutputDescription(InventoryProcessPartUtils.createProcessPartDescription(item));
            }
        } finally {
            for (EntityRef outputItem : items) {
                outputItem.destroy();
            }
        }
    }
}
