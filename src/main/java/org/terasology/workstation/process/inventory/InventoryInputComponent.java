package org.terasology.workstation.process.inventory;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.layouts.FlowLayout;
import org.terasology.workstation.process.DescribeProcess;
import org.terasology.workstation.process.ErrorCheckingProcessPart;
import org.terasology.workstation.process.InvalidProcessPartException;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.ProcessPartDescription;
import org.terasology.workstation.process.ProcessPartOrdering;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.ui.InventoryItem;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public abstract class InventoryInputComponent implements Component, ProcessPart, ValidateInventoryItem, DescribeProcess, ErrorCheckingProcessPart, ProcessPartOrdering {
    private static final Logger logger = LoggerFactory.getLogger(InventoryInputComponent.class);

    protected abstract Map<Predicate<EntityRef>, Integer> getInputItems();

    protected abstract Set<EntityRef> createItems();

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
        final List<EntityRef> itemCopies = Lists.newArrayList();

        for (Map.Entry<Predicate<EntityRef>, Integer> requiredItem : getInputItems().entrySet()) {
            Predicate<EntityRef> filter = requiredItem.getKey();

            int remainingToFind = requiredItem.getValue();
            boolean foundItem = false;

            for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "INPUT")) {
                EntityRef item = InventoryUtils.getItemAt(workstation, slot);
                if (filter.apply(item)) {
                    foundItem = true;

                    EntityRef itemCopy = item.copy();
                    ItemComponent itemComponent = itemCopy.getComponent(ItemComponent.class);
                    itemComponent.stackCount = (byte) Math.min(remainingToFind, itemComponent.stackCount);
                    itemCopy.saveComponent(itemComponent);
                    itemCopies.add(itemCopy);

                    remainingToFind -= itemComponent.stackCount;
                    if (remainingToFind == 0) {
                        break;
                    }
                }
            }

            if (!foundItem || remainingToFind > 0) {
                return false;
            }

        }

        InventoryInputProcessPartItemsComponent inputItemsComponent = processEntity.getComponent(InventoryInputProcessPartItemsComponent.class);
        if (inputItemsComponent == null) {
            processEntity.addComponent(new InventoryInputProcessPartItemsComponent(itemCopies));
        } else {
            inputItemsComponent.items.addAll(itemCopies);
            processEntity.saveComponent(inputItemsComponent);
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
            removeItem(instigator, workstation, requiredItem.getKey(), requiredItem.getValue(), processEntity);
        }
    }

    private void removeItem(EntityRef instigator, EntityRef workstation, Predicate<EntityRef> filter, int toRemove, EntityRef processEntity) {
        int remainingToRemove = toRemove;

        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "INPUT")) {
            EntityRef item = InventoryUtils.getItemAt(workstation, slot);
            if (filter.apply(item)) {
                int remove = Math.min(InventoryUtils.getStackCount(item), remainingToRemove);
                EntityRef removedItem = CoreRegistry.get(InventoryManager.class).removeItem(workstation, instigator, item, false, remove);
                if (removedItem != null)
                    remainingToRemove -= remove;
                if (remainingToRemove == 0) {
                    break;
                }
            }
        }
    }

    @Override
    public int getSortOrder() {
        return -1;
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
    }

    @Override
    public ProcessPartDescription getInputDescription() {
        Set<EntityRef> items = createItems();
        Set<String> descriptions = Sets.newHashSet();
        FlowLayout flowLayout = new FlowLayout();
        try {
            for (EntityRef item : items) {
                int stackCount = InventoryUtils.getStackCount(item);
                DisplayNameComponent displayNameComponent = item.getComponent(DisplayNameComponent.class);
                if (displayNameComponent != null) {
                    descriptions.add(stackCount + " " + displayNameComponent.name);
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
    public ProcessPartDescription getOutputDescription() {
        return null;
    }

    @Override
    public int getComplexity() {
        return 0;
    }

    @Override
    public void checkForErrors() throws InvalidProcessPartException {
        Set<EntityRef> items = null;
        try {
            items = createItems();
            if (items.size() == 0) {
                throw new InvalidProcessPartException("No input items specified");
            }
        } catch (Exception ex) {
            throw new InvalidProcessPartException("Could not create input items");
        } finally {
            if (items != null) {
                for (EntityRef outputItem : items) {
                    outputItem.destroy();
                }
            }
        }
    }
}
