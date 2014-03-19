package org.terasology.workstation.process.inventory;

import com.google.common.base.Predicate;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.registry.CoreRegistry;
import org.terasology.workstation.process.DescribeProcess;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.WorkstationInventoryUtils;

import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public abstract class InventoryInputComponent implements Component, ProcessPart, ValidateInventoryItem, DescribeProcess {
    protected abstract Map<Predicate<EntityRef>, Integer> getInputItems();

    @Override
    public boolean isResponsibleForSlot(EntityRef workstation, int slotNo) {
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "INPUT")) {
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
        for (Map.Entry<Predicate<EntityRef>, Integer> requiredItem : getInputItems().entrySet()) {
            Predicate<EntityRef> filter = requiredItem.getKey();

            int foundCount = 0;
            boolean foundItem = false;

            for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "INPUT")) {
                EntityRef item = InventoryUtils.getItemAt(workstation, slot);
                if (filter.apply(item)) {
                    foundItem = true;
                    foundCount += InventoryUtils.getStackCount(item);
                }
            }

            if (foundItem) {
                return requiredItem.getValue() <= foundCount;
            } else {
                return false;
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
        for (Map.Entry<Predicate<EntityRef>, Integer> requiredItem : getInputItems().entrySet()) {
            removeItem(instigator, workstation, requiredItem.getKey(), requiredItem.getValue());
        }
    }

    private void removeItem(EntityRef instigator, EntityRef workstation, Predicate<EntityRef> filter, int toRemove) {
        int remainingToRemove = toRemove;
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "INPUT")) {
            EntityRef item = InventoryUtils.getItemAt(workstation, slot);
            if (filter.apply(item)) {
                int remove = Math.min(InventoryUtils.getStackCount(item), remainingToRemove);
                if (CoreRegistry.get(InventoryManager.class).removeItem(workstation, instigator, item, true, remove) != null) {
                    remainingToRemove -= remove;
                    if (remainingToRemove == 0) {
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public int getComplexity() {
        return 0;
    }
}
