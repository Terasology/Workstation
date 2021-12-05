// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process.inventory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.block.items.BlockItemComponent;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.module.inventory.systems.InventoryUtils;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.processPart.ProcessEntityIsInvalidEvent;
import org.terasology.workstation.processPart.ProcessEntityIsInvalidToStartEvent;
import org.terasology.workstation.processPart.ProcessEntityStartExecutionEvent;
import org.terasology.workstation.processPart.inventory.ProcessEntityIsInvalidForInventoryItemEvent;
import org.terasology.workstation.processPart.metadata.ProcessEntityGetInputDescriptionEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This system will:
 * On ProcessEntityIsInvalidToStartEvent
 * - find all item slots and save a InventoryInputProcessPartSlotAmountsComponent to the process entity
 * On ProcessEntityStartExecutionEvent
 * - use the slot amounts from the validation event to remove items from the inventory and add them to a InventoryInputItemsComponent saved on the proces entity.
 * - the items added to InventoryInputItemsComponent will be destroyed when the process entity is destroyed
 */
@RegisterSystem
public class InventoryInputProcessPartCommonSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(InventoryInputProcessPartCommonSystem.class);
    public static final String WORKSTATIONINPUTCATEGORY = "INPUT";

    @In
    InventoryManager inventoryManager;
    @In
    BlockManager blockManager;
    @In
    EntityManager entityManager;

    @ReceiveEvent
    public void validateProcess(ProcessEntityIsInvalidEvent event, EntityRef processEntity,
                                InventoryInputComponent inventoryInputComponent) {

        Set<EntityRef> items = InventoryProcessPartUtils.createItems(inventoryInputComponent.blockCounts,
                inventoryInputComponent.itemCounts, false, entityManager, blockManager);
        try {
            if (items.size() == 0) {
                event.addError("No input items specified in " + this.getClass().getSimpleName());
            }
        } catch (Exception ex) {
            event.addError("Could not create input items in " + this.getClass().getSimpleName());
        } finally {
            if (items != null) {
                for (EntityRef item : items) {
                    item.destroy();
                }
            }
        }
    }

    @ReceiveEvent
    public void validateToStartExecution(ProcessEntityIsInvalidToStartEvent event, EntityRef processEntity,
                                         InventoryInputComponent inventoryInputComponent) {
        Map<Predicate<EntityRef>, Integer> itemFilters = getBlockFamilyItemsPredicateMap(inventoryInputComponent);
        Map<Integer, Integer> slotAmounts = InventoryProcessPartUtils.findItems(event.getWorkstation(), WORKSTATIONINPUTCATEGORY,
                itemFilters, processEntity, event.getInstigator());
        if (slotAmounts != null) {
            processEntity.addComponent(new InventoryInputProcessPartSlotAmountsComponent(slotAmounts));
            Set<EntityRef> inputItems = Sets.newHashSet();
            for (Map.Entry<Integer, Integer> slotAmount : slotAmounts.entrySet()) {
                EntityRef item = InventoryUtils.getItemAt(event.getWorkstation(), slotAmount.getKey());
                EntityRef copiedItem = item.copy();
                ItemComponent itemComponent = copiedItem.getComponent(ItemComponent.class);
                itemComponent.stackCount = slotAmount.getValue().byteValue();
                copiedItem.saveComponent(itemComponent);
                inputItems.add(copiedItem);
            }
            processEntity.addComponent(new InventoryInputItemsComponent(inputItems));
            processEntity.removeComponent(InventoryInputItemsComponent.class);
            for (EntityRef inputItem : inputItems) {
                inputItem.destroy();
            }
        } else {
            event.consume();
        }
    }

    private Map<Predicate<EntityRef>, Integer> getBlockFamilyItemsPredicateMap(InventoryInputComponent inventoryInputComponent) {
        Map<Predicate<EntityRef>, Integer> result = new HashMap<>();
        for (Map.Entry<String, Integer> blockFamilyCount : inventoryInputComponent.blockCounts.entrySet()) {
            result.put(new BlockFamilyPredicate(new BlockUri(blockFamilyCount.getKey())), blockFamilyCount.getValue());
        }

        for (Map.Entry<String, Integer> itemCount : inventoryInputComponent.itemCounts.entrySet()) {
            result.put(new ItemPrefabPredicate(Assets.getPrefab(itemCount.getKey()).get().getUrn()), itemCount.getValue());
        }
        return result;
    }

    @ReceiveEvent
    public void execute(ProcessEntityStartExecutionEvent event, EntityRef processEntity,
                        InventoryInputComponent inventoryInputComponent) {
        InventoryInputProcessPartSlotAmountsComponent slotAmountsComponent =
                processEntity.getComponent(InventoryInputProcessPartSlotAmountsComponent.class);
        // this will be null if another process part has already consumed the items
        if (slotAmountsComponent != null) {
            InventoryInputItemsComponent inventoryInputItemsComponent = new InventoryInputItemsComponent();
            for (Map.Entry<Integer, Integer> slotAmount : slotAmountsComponent.slotAmounts.entrySet()) {
                EntityRef item = InventoryUtils.getItemAt(event.getWorkstation(), slotAmount.getKey());
                if (slotAmount.getValue() > InventoryUtils.getStackCount(item)) {
                    logger.error("Not enough items in the stack");
                }
                EntityRef removedItem = inventoryManager.removeItem(event.getWorkstation(), event.getInstigator(), item, false,
                        slotAmount.getValue());
                inventoryInputItemsComponent.items.add(removedItem);
                if (removedItem == null) {
                    logger.error("Could not remove input item");
                }
            }

            // add the removed items to the process entity.  They will be destroyed along with the process entity eventually unless
            // removed from the component.
            processEntity.addComponent(inventoryInputItemsComponent);
        }


        // remove the slot amounts from the process entity, no other InventoryInput should use it
        processEntity.removeComponent(InventoryInputProcessPartSlotAmountsComponent.class);
    }

    @ReceiveEvent
    public void validateInventoryItem(ProcessEntityIsInvalidForInventoryItemEvent event, EntityRef processEntity,
                                      InventoryInputComponent inventoryInputComponent) {
        if (WorkstationInventoryUtils.getAssignedInputSlots(event.getWorkstation(), WORKSTATIONINPUTCATEGORY).contains(event.getSlotNo())
                && !Iterables.any(getBlockFamilyItemsPredicateMap(inventoryInputComponent).keySet(), x -> x.apply(event.getItem()))) {
            event.consume();
        }
    }

    @ReceiveEvent
    public void getInputDescriptions(ProcessEntityGetInputDescriptionEvent event, EntityRef processEntity,
                                     InventoryInputComponent inventoryInputComponent) {
        Set<EntityRef> items = InventoryProcessPartUtils.createItems(inventoryInputComponent.blockCounts,
                inventoryInputComponent.itemCounts, false, entityManager, blockManager);
        try {
            for (EntityRef item : items) {
                event.addInputDescription(InventoryProcessPartUtils.createProcessPartDescription(item));
            }
        } finally {
            for (EntityRef outputItem : items) {
                outputItem.destroy();
            }
        }
    }

    private static final class BlockFamilyPredicate implements Predicate<EntityRef> {
        private BlockUri blockFamilyUri;

        private BlockFamilyPredicate(BlockUri blockFamilyUri) {
            this.blockFamilyUri = blockFamilyUri;
        }

        @Override
        public boolean apply(EntityRef input) {
            BlockItemComponent blockItem = input.getComponent(BlockItemComponent.class);
            if (blockItem == null) {
                return false;
            }
            return blockItem.blockFamily.getURI().equals(blockFamilyUri);
        }
    }

    private static final class ItemPrefabPredicate implements Predicate<EntityRef> {
        private ResourceUrn prefab;

        private ItemPrefabPredicate(ResourceUrn prefab) {
            this.prefab = prefab;
        }

        @Override
        public boolean apply(EntityRef input) {
            ItemComponent item = input.getComponent(ItemComponent.class);
            if (item == null) {
                return false;
            }
            return input.getParentPrefab().getUrn().equals(prefab);
        }
    }
}
