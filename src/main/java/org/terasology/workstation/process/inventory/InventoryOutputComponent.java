package org.terasology.workstation.process.inventory;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.registry.CoreRegistry;
import org.terasology.workstation.process.DescribeProcess;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.WorkstationInventoryUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public abstract class InventoryOutputComponent implements Component, ProcessPart, ValidateInventoryItem, DescribeProcess {
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
    public String getInputDescription() {
        return null;
    }

    @Override
    public String getOutputDescription() {
        Set<EntityRef> items = createOutputItems();
        Set<String> descriptions = Sets.newHashSet();
        try {
            for (EntityRef item : items) {
                int stackCount = InventoryUtils.getStackCount(item);
                descriptions.add(stackCount + " " + item.getComponent(DisplayNameComponent.class).name);
            }
        } finally {
            for (EntityRef outputItem : items) {
                outputItem.destroy();
            }
        }

        return Joiner.on(", ").join(descriptions);
    }

    @Override
    public int getComplexity() {
        return 0;
    }


}
