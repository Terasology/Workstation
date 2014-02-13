package org.terasology.workstation.process.inventory;

import com.google.common.base.Predicate;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.action.RemoveItemAction;
import org.terasology.workstation.component.WorkstationInventoryComponent;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.ProcessPart;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public abstract class InventoryInputComponent implements Component, ProcessPart {
    protected abstract Map<Predicate<EntityRef>, Integer> getInputItems();

    private Collection<Integer> getInputSlots(EntityRef workstation) {
        WorkstationInventoryComponent inventory = workstation.getComponent(WorkstationInventoryComponent.class);
        return Collections.unmodifiableCollection(inventory.slotAssignments.get("INPUT"));
    }

    @Override
    public Set<String> validate(EntityRef instigator, EntityRef workstation) throws InvalidProcessException {
        for (Map.Entry<Predicate<EntityRef>, Integer> requiredItem : getInputItems().entrySet()) {
            Predicate<EntityRef> filter = requiredItem.getKey();

            int foundCount = 0;

            for (int slot : getInputSlots(workstation)) {
                EntityRef item = InventoryUtils.getItemAt(workstation, slot);
                if (filter.apply(item)) {
                    foundCount += InventoryUtils.getStackCount(item);
                }
            }

            if (requiredItem.getValue() > foundCount) {
                throw new InvalidProcessException();
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
        for (Map.Entry<Predicate<EntityRef>, Integer> requiredItem : getInputItems().entrySet()) {
            removeItem(instigator, workstation, requiredItem.getKey(), requiredItem.getValue());
        }
    }

    private void removeItem(EntityRef instigator, EntityRef workstation, Predicate<EntityRef> filter, int toRemove) {
        for (int slot : getInputSlots(workstation)) {
            EntityRef item = InventoryUtils.getItemAt(workstation, slot);
            if (filter.apply(item)) {
                int remove = Math.min(InventoryUtils.getStackCount(item), toRemove);
                workstation.send(new RemoveItemAction(instigator, item, true, remove));
                toRemove -= remove;
                if (toRemove == 0) {
                    return;
                }
            }
        }
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, String result) {
    }
}
