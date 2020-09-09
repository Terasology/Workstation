// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process.inventory;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.items.BlockItemComponent;
import org.terasology.engine.world.block.items.BlockItemFactory;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.inventory.logic.InventoryUtils;
import org.terasology.workstation.process.ProcessPartDescription;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.processPart.inventory.ProcessEntityIsInvalidForInventoryItemEvent;
import org.terasology.workstation.ui.InventoryItem;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class InventoryProcessPartUtils {
    private InventoryProcessPartUtils() {
    }

    public static Set<EntityRef> createItems(Map<String, Integer> blockCounts, Map<String, Integer> itemCounts,
                                             boolean createPersistentEntities, EntityManager entityManager,
                                             BlockManager blockManager) {
        BlockItemFactory itemFactory = new BlockItemFactory(entityManager);
        Set<EntityRef> result = new HashSet<>();
        // create blocks
        for (Map.Entry<String, Integer> blockCount : blockCounts.entrySet()) {
            BlockFamily blockFamily = blockManager.getBlockFamily(blockCount.getKey());
            EntityBuilder entityBuilder = itemFactory.newBuilder(blockFamily, blockCount.getValue());
            entityBuilder.setPersistent(createPersistentEntities);
            result.add(entityBuilder.build());
        }

        // create items
        for (Map.Entry<String, Integer> itemCount : itemCounts.entrySet()) {
            EntityBuilder entityBuilder = entityManager.newBuilder(itemCount.getKey());
            entityBuilder.setPersistent(createPersistentEntities);

            ItemComponent item = entityBuilder.getComponent(ItemComponent.class);

            // Only set the stack count if the ItemComponent exists.
            if (item != null) {
                item.stackCount = itemCount.getValue().byteValue();
                entityBuilder.saveComponent(item);
                result.add(entityBuilder.build());
            }
        }

        return result;
    }

    /**
     * Selects matching items in a workstation.  If the items are not all found,  return  null. Also will check to
     * ensure no other systems reject this item
     *
     * @param workstation
     * @param workstationInventoryCategory
     * @param inputItems
     * @return null if not all found
     */
    public static Map<Integer, Integer> findItems(EntityRef workstation, String workstationInventoryCategory,
                                                  Map<Predicate<EntityRef>, Integer> inputItems,
                                                  EntityRef processEntity, EntityRef instigator) {
        final Map<Integer, Integer> slotAmounts = Maps.newHashMap();

        for (Map.Entry<Predicate<EntityRef>, Integer> requiredItem : inputItems.entrySet()) {
            Predicate<EntityRef> filter = requiredItem.getKey();
            int remainingToFind = requiredItem.getValue();
            boolean foundItem = false;
            for (int slot : WorkstationInventoryUtils.getAssignedInputSlots(workstation,
                    workstationInventoryCategory)) {
                EntityRef item = InventoryUtils.getItemAt(workstation, slot);
                if (filter.apply(item)) {
                    // make sure no other systems reject this item
                    ProcessEntityIsInvalidForInventoryItemEvent validationEvent =
                            new ProcessEntityIsInvalidForInventoryItemEvent(workstation, slot, instigator, item);
                    processEntity.send(validationEvent);
                    if (validationEvent.isConsumed()) {
                        continue;
                    }

                    // this item is good to go
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
                return null;
            }
        }

        return slotAmounts;
    }

    public static boolean canGiveItemsTo(EntityRef workstation, Set<EntityRef> outputItems,
                                         String workstationInventoryCategory) {
        try {
            Set<EntityRef> itemsLeftToAssign = new HashSet<>(outputItems);
            int emptySlots = 0;

            for (int slot : WorkstationInventoryUtils.getAssignedOutputSlots(workstation,
                    workstationInventoryCategory)) {
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

    public static ProcessPartDescription createProcessPartDescription(EntityRef item) {
        ResourceUrn resourceUrn = item.getParentPrefab().getUrn();
        // Treat blocks differently as they have special rules
        BlockItemComponent blockItemComponent = item.getComponent(BlockItemComponent.class);
        if (blockItemComponent != null) {
            resourceUrn = blockItemComponent.blockFamily.getURI().getBlockFamilyDefinitionUrn();
        }

        int stackCount = InventoryUtils.getStackCount(item);
        String displayName = "";
        DisplayNameComponent displayNameComponent = item.getComponent(DisplayNameComponent.class);
        if (displayNameComponent != null) {
            displayName = displayNameComponent.name;
        }
        return new ProcessPartDescription(resourceUrn, stackCount + " " + displayName, new InventoryItem(item));
    }
}
