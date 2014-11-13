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
package org.terasology.workstation.process.inventory;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.layouts.FlowLayout;
import org.terasology.workstation.process.DescribeProcess;
import org.terasology.workstation.process.ErrorCheckingProcessPart;
import org.terasology.workstation.process.InvalidProcessPartException;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.ProcessPartDescription;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.ui.InventoryItem;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public abstract class InventoryOutputComponent implements Component, ProcessPart, ValidateInventoryItem, DescribeProcess, ErrorCheckingProcessPart {
    private static final Logger logger = LoggerFactory.getLogger(InventoryOutputComponent.class);

    protected abstract Set<EntityRef> createOutputItems();

    @Override
    public boolean isResponsibleForSlot(EntityRef workstation, int slotNo) {
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT")) {
            if (slot == slotNo) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValid(EntityRef workstation, int slotNo, EntityRef instigator, EntityRef item) {
        return workstation == instigator;
    }

    @Override
    public boolean validateBeforeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        Set<EntityRef> outputItems = createOutputItems();
        try {
            Set<EntityRef> itemsLeftToAssign = new HashSet<>(outputItems);
            int emptySlots = 0;

            for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT")) {
                EntityRef item = InventoryUtils.getItemAt(workstation, slot);
                if (item.exists()) {
                    for (EntityRef itemLeftToAssign : itemsLeftToAssign) {
                        if (InventoryUtils.canStackInto(itemLeftToAssign, item)) {
                            itemsLeftToAssign.remove(itemLeftToAssign);
                            break;
                        }
                    }
                } else {
                    emptySlots++;
                }
            }

            if (emptySlots < itemsLeftToAssign.size()) {
                return false;
            }
        } finally {
            for (EntityRef outputItem : outputItems) {
                outputItem.destroy();
            }
        }

        return true;
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        return 0;
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        Set<EntityRef> outputItems = createOutputItems();

        for (EntityRef outputItem : outputItems) {
            addItemToInventory(instigator, workstation, outputItem);
        }
    }

    private void addItemToInventory(EntityRef instigator, EntityRef workstation, EntityRef outputItem) {
        if (!CoreRegistry.get(InventoryManager.class).giveItem(workstation, instigator, outputItem, WorkstationInventoryUtils.getAssignedSlots(workstation, "OUTPUT"))) {
            outputItem.destroy();
        }
    }

    @Override
    public ProcessPartDescription getInputDescription() {
        return null;
    }

    @Override
    public ProcessPartDescription getOutputDescription() {
        Set<EntityRef> items = createOutputItems();
        Set<String> descriptions = Sets.newHashSet();
        FlowLayout flowLayout = new FlowLayout();
        try {
            for (EntityRef item : items) {
                int stackCount = InventoryUtils.getStackCount(item);
                DisplayNameComponent displayNameComponent = item.getComponent(DisplayNameComponent.class);
                if (displayNameComponent != null) {
                    descriptions.add(stackCount + " " + item.getComponent(DisplayNameComponent.class).name);
                    flowLayout.addWidget(new InventoryItem(item), null);
                } else {
                    logger.error(item.toString() + " DisplayNameComponent not found");
                }
            }
        } finally {
            for (EntityRef outputItem : items) {
                outputItem.destroy();
            }
        }

        return new ProcessPartDescription(Joiner.on(", ").join(descriptions), flowLayout);
    }

    @Override
    public int getComplexity() {
        return 0;
    }


    @Override
    public void checkForErrors() throws InvalidProcessPartException {
        Set<EntityRef> items = null;
        try {
            items = createOutputItems();
            if (items.size() == 0) {
                throw new InvalidProcessPartException("No output items specified");
            }
        } catch (Exception ex) {
            throw new InvalidProcessPartException("Could not create output items");
        } finally {
            if (items != null) {
                for (EntityRef outputItem : items) {
                    outputItem.destroy();
                }
            }
        }
    }
}
