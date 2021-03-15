/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.workstation.system;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.engine.registry.In;
import org.terasology.logic.inventory.events.BeforeItemPutInInventory;
import org.terasology.workstation.component.WorkstationComponent;
import org.terasology.workstation.process.WorkstationProcess;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class WorkstationInventoryValidationSystem extends BaseComponentSystem {
    @In
    private WorkstationRegistry workstationRegistry;

    @ReceiveEvent
    public void itemPutIntoWorkstation(BeforeItemPutInInventory event, EntityRef entity,
                                       WorkstationComponent workstation, InventoryComponent workstationInventory) {
        int slot = event.getSlot();

        boolean hasValidation = false;
        for (WorkstationProcess workstationProcess : workstationRegistry.getWorkstationProcesses(workstation.supportedProcessTypes.keySet())) {
            if (workstationProcess instanceof ValidateInventoryItem) {
                ValidateInventoryItem inventoryValidator = (ValidateInventoryItem) workstationProcess;
                hasValidation = true;
                if (inventoryValidator.isValid(entity, slot, event.getInstigator(), event.getItem())) {
                    return;
                }
            }
        }

        if (hasValidation) {
            // There were validators, but no process has accepted this item
            event.consume();
        }
    }
}
