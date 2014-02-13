package org.terasology.workstation.process.inventory;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.action.GiveItemAction;
import org.terasology.workstation.component.WorkstationInventoryComponent;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.ProcessPart;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public abstract class InventoryOutputComponent implements Component, ProcessPart {
    protected abstract Set<EntityRef> createOutputItems();

    private Collection<Integer> getOutputSlots(EntityRef workstation) {
        WorkstationInventoryComponent inventory = workstation.getComponent(WorkstationInventoryComponent.class);
        return Collections.unmodifiableCollection(inventory.slotAssignments.get("OUTPUT"));
    }

    @Override
    public Set<String> validate(EntityRef instigator, EntityRef workstation) throws InvalidProcessException {
        Set<EntityRef> outputItems = createOutputItems();
        try {
            Set<EntityRef> itemsLeftToAssign = new HashSet<>(outputItems);
            int emptySlots = 0;

            for (int slot : getOutputSlots(workstation)) {
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
                throw new InvalidProcessException();
            }
        } finally {
            for (EntityRef outputItem : outputItems) {
                outputItem.destroy();
            }
        }

        return null;
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, String result) {
        return 0;
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, String result) {
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, String result) {
        Set<EntityRef> outputItems = createOutputItems();

        for (EntityRef outputItem : outputItems) {
            addItemToInventory(instigator, workstation, outputItem);
        }
    }

    private void addItemToInventory(EntityRef instigator, EntityRef workstation, EntityRef outputItem) {
        // First try to merge into existing slots
        for (int slot : getOutputSlots(workstation)) {
            EntityRef item = InventoryUtils.getItemAt(workstation, slot);
            if (item.exists()) {
                if (InventoryUtils.canStackInto(outputItem, item)) {
                    GiveItemAction event = new GiveItemAction(instigator, outputItem, slot);
                    workstation.send(event);
                    if (!event.isConsumed()) {
                        outputItem.destroy();
                    }
                    return;
                }
            }
        }

        // Then fill out empty slots
        for (int slot : getOutputSlots(workstation)) {
            EntityRef item = InventoryUtils.getItemAt(workstation, slot);
            if (!item.exists()) {
                GiveItemAction event = new GiveItemAction(instigator, outputItem, slot);
                workstation.send(event);
                if (!event.isConsumed()) {
                    outputItem.destroy();
                }
                return;
            }
        }
    }
}
