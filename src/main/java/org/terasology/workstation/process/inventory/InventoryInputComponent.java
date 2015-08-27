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

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.layouts.FlowLayout;
import org.terasology.workstation.process.DescribeProcess;
import org.terasology.workstation.process.ErrorCheckingProcessPart;
import org.terasology.workstation.process.InvalidProcessPartException;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.ProcessPartDescription;
import org.terasology.workstation.process.ProcessPartOrdering;
import org.terasology.workstation.process.WorkstationInventoryUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public abstract class InventoryInputComponent implements Component, ProcessPart, ValidateInventoryItem, DescribeProcess, ErrorCheckingProcessPart, ProcessPartOrdering {
    public static final String WORKSTATIONINPUTCATEGORY = "INPUT";
    public static final int SORTORDER = -1;
    private static final Logger logger = LoggerFactory.getLogger(InventoryInputComponent.class);

    protected abstract Map<Predicate<EntityRef>, Integer> getInputItems();

    protected abstract Set<EntityRef> createItems();

    @Override
    public boolean isResponsibleForSlot(EntityRef workstation, int slotNo) {
        for (int slot : WorkstationInventoryUtils.getAssignedInputSlots(workstation, WORKSTATIONINPUTCATEGORY)) {
            if (slot == slotNo) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isValid(EntityRef workstation, int slotNo, EntityRef instigator, EntityRef item) {
        for (Predicate<EntityRef> inputPredicate : getInputItems().keySet()) {
            if (inputPredicate.apply(item)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean validateBeforeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        // Select the items to consume and save it to the process entity using the InventoryInputProcessPartSlotAmountsComponent
        final Map<Integer, Integer> slotAmounts = Maps.newHashMap();

        for (Map.Entry<Predicate<EntityRef>, Integer> requiredItem : getInputItems().entrySet()) {
            Predicate<EntityRef> filter = requiredItem.getKey();
            int remainingToFind = requiredItem.getValue();
            boolean foundItem = false;
            for (int slot : WorkstationInventoryUtils.getAssignedInputSlots(workstation, WORKSTATIONINPUTCATEGORY)) {
                EntityRef item = InventoryUtils.getItemAt(workstation, slot);
                if (filter.apply(item)) {
                    foundItem = true;
                    int amountToUse = (byte) Math.min(remainingToFind, InventoryUtils.getStackCount(item));
                    slotAmounts.put(slot, amountToUse);
                    remainingToFind -= amountToUse;
                    if (remainingToFind == 0) {
                        break;
                    }
                }
            }

            if (!foundItem || remainingToFind > 0) {
                return false;
            }

        }

        InventoryInputProcessPartSlotAmountsComponent slotAmountsComponent = processEntity.getComponent(InventoryInputProcessPartSlotAmountsComponent.class);
        if (slotAmountsComponent == null) {
            processEntity.addComponent(new InventoryInputProcessPartSlotAmountsComponent(slotAmounts));
        } else {
            slotAmountsComponent.slotAmounts.putAll(slotAmounts);
            processEntity.saveComponent(slotAmountsComponent);
        }

        return true;
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        return 0;
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        InventoryInputProcessPartSlotAmountsComponent inputItems = processEntity.getComponent(InventoryInputProcessPartSlotAmountsComponent.class);
        // this will be null if another process part has already consumed the items
        if (inputItems != null) {
            for (Map.Entry<Integer, Integer> slotAmount : inputItems.slotAmounts.entrySet()) {
                EntityRef item = InventoryUtils.getItemAt(workstation, slotAmount.getKey());
                if (slotAmount.getValue() > InventoryUtils.getStackCount(item)) {
                    logger.error("Not enough items in the stack");
                }
                EntityRef removedItem = CoreRegistry.get(InventoryManager.class).removeItem(workstation, instigator, item, false, slotAmount.getValue());
                if (removedItem == null) {
                    logger.error("Could not remove input item");
                }
            }
        }

        // remove the slot amounts from the process entity, no other InventoryInput should use it
        processEntity.removeComponent(InventoryInputProcessPartSlotAmountsComponent.class);
    }

    @Override
    public int getSortOrder() {
        return SORTORDER;
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
    }

    @Override
    public Collection<ProcessPartDescription> getInputDescriptions() {
        List<ProcessPartDescription> descriptions = Lists.newLinkedList();
        Set<EntityRef> items = createItems();
        FlowLayout flowLayout = new FlowLayout();
        try {
            for (EntityRef item : items) {
                descriptions.add(InventoryOutputComponent.createProcessPartDescription(item));
            }
        } finally {
            for (EntityRef outputItem : items) {
                outputItem.destroy();
            }
        }

        return descriptions;
    }

    @Override
    public Collection<ProcessPartDescription> getOutputDescriptions() {
        return Collections.emptyList();
    }

    @Override
    public void checkForErrors() throws InvalidProcessPartException {
        Set<EntityRef> items = null;
        try {
            items = createItems();
            if (items.size() == 0) {
                throw new InvalidProcessPartException("No input items specified in " + this.getClass().getSimpleName());
            }
        } catch (Exception ex) {
            throw new InvalidProcessPartException("Could not create input items in " + this.getClass().getSimpleName());
        } finally {
            if (items != null) {
                for (EntityRef outputItem : items) {
                    outputItem.destroy();
                }
            }
        }
    }
}
