// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process.fluid;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.fluid.component.FluidComponent;
import org.terasology.fluid.component.FluidContainerItemComponent;
import org.terasology.fluid.component.FluidInventoryComponent;
import org.terasology.fluid.event.BeforeFluidPutInInventory;
import org.terasology.fluid.system.FluidManager;
import org.terasology.fluid.system.FluidUtils;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.module.inventory.systems.InventoryUtils;
import org.terasology.workstation.component.SpecificInputSlotComponent;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.processPart.ProcessEntityIsInvalidToStartEvent;
import org.terasology.workstation.processPart.ProcessEntityStartExecutionEvent;
import org.terasology.workstation.processPart.inventory.ProcessEntityIsInvalidForInventoryItemEvent;

@RegisterSystem
public class FillFluidInventoryPartProcessPartCommonSystem extends BaseComponentSystem {
    public static final float DELTA = 0.001f;

    @In
    FluidManager fluidManager;

    /**
     * Validate the process to ensure that it's correct and ready for execution.
     *
     * @param event The event which has a reference to the workstation and instigator.
     * @param processEntity The reference to the process that's being verified.
     * @param fillFluidInventoryPart A component included for filtering out non-matching events. Here, we only want processes
     *         related to filling an inventory slot with fluid.
     */
    @ReceiveEvent
    public void validateToStartExecution(ProcessEntityIsInvalidToStartEvent event, EntityRef processEntity,
                                         FillFluidInventoryPart fillFluidInventoryPart) {
        FluidInventoryComponent fluidInventory = event.getWorkstation().getComponent(FluidInventoryComponent.class);

        for (int containerInputSlot : WorkstationInventoryUtils.getAssignedSlots(event.getWorkstation(), "FLUID_CONTAINER_INPUT")) {
            EntityRef containerItem = InventoryUtils.getItemAt(event.getWorkstation(), containerInputSlot);
            if (canEmptyContainerItem(event.getWorkstation(), fluidInventory, containerItem)) {
                processEntity.addComponent(new SpecificInputSlotComponent(containerInputSlot));
                return;
            }
        }

        event.consume();
    }

    /**
     * Start execution of the fluid inventory slot filling process.
     *
     * @param event The event which has a reference to the workstation and instigator.
     * @param processEntity The reference to the process being executed.
     * @param fillFluidInventoryPart A component included for filtering out non-matching events. Here, we only want processes
     *         related to filling inventory slots with fluid.
     */
    @ReceiveEvent
    public void startExecution(ProcessEntityStartExecutionEvent event, EntityRef processEntity,
                               FillFluidInventoryPart fillFluidInventoryPart) {
        SpecificInputSlotComponent inputSlot = processEntity.getComponent(SpecificInputSlotComponent.class);

        EntityRef containerItem = InventoryUtils.getItemAt(event.getWorkstation(), inputSlot.slot);
        FluidContainerItemComponent fluidContainer = containerItem.getComponent(FluidContainerItemComponent.class);
        boolean transferredFromHolder = false; // Indicate whether addFluidFromHolder (true) or addFluid (false) was used.

        for (int fluidSlot : WorkstationInventoryUtils.getAssignedSlots(event.getWorkstation(), "FLUID_INPUT")) {
            if (fluidManager.addFluidFromHolder(event.getInstigator(), event.getWorkstation(), containerItem, fluidSlot, fluidContainer.fluidType, fluidContainer.volume)) {
                transferredFromHolder = true;
                break;
            }
        }

        final EntityRef removedItem = CoreRegistry.get(InventoryManager.class).removeItem(event.getWorkstation(), event.getInstigator(), containerItem, false, 1);
        if (removedItem != null) {
            // Don't set the fluid to null unless it's empty or it has not been transferred.
            if (!transferredFromHolder || fluidContainer.volume <= DELTA) {
                FluidUtils.setFluidForContainerItem(removedItem, null);
            }

            // Transfer the used fluid container to the workstation's FLUID_CONTAINER_OUTPUT slot.
            if (CoreRegistry.get(InventoryManager.class).giveItem(event.getWorkstation(), event.getInstigator(), removedItem,
                    WorkstationInventoryUtils.getAssignedSlots(event.getWorkstation(), "FLUID_CONTAINER_OUTPUT"))) {
                return;
            }

            removedItem.destroy();
        }
    }

    ///// Inventory

    /**
     * Verify if the provided fluid container(s) and fluid inventory are valid for this process.
     *
     * @param event The event which has a reference to the workstation, slot number, item, and instigator.
     * @param processEntity The reference to the process that's intending to use these items.
     * @param fillFluidInventoryPart A component included for filtering out non-matching events. Here, we only want processes
     *         related to filling an inventory slot with fluid.
     */
    @ReceiveEvent
    public void isValidInventoryItem(ProcessEntityIsInvalidForInventoryItemEvent event, EntityRef processEntity,
                                     FillFluidInventoryPart fillFluidInventoryPart) {
        if (WorkstationInventoryUtils.getAssignedSlots(event.getWorkstation(), "FLUID_CONTAINER_INPUT").contains(event.getSlotNo())
                || WorkstationInventoryUtils.getAssignedSlots(event.getWorkstation(), "FLUID_CONTAINER_OUTPUT").contains(event.getSlotNo())) {
            for (int slot : WorkstationInventoryUtils.getAssignedSlots(event.getWorkstation(), "FLUID_CONTAINER_INPUT")) {
                if (slot == event.getSlotNo()) {
                    FluidContainerItemComponent fluidContainer = event.getItem().getComponent(FluidContainerItemComponent.class);
                    if (fluidContainer == null) {
                        event.consume();
                        return;
                    }
                    if (fluidContainer.fluidType == null) {
                        event.consume();
                        return;
                    }

                    FluidInventoryComponent fluidInventory = event.getWorkstation().getComponent(FluidInventoryComponent.class);
                    if (fluidInventory == null) {
                        event.consume();
                        return;
                    }

                    for (int fluidSlot : WorkstationInventoryUtils.getAssignedSlots(event.getWorkstation(), "FLUID_INPUT")) {
                        BeforeFluidPutInInventory beforeFluidAdded = new BeforeFluidPutInInventory(event.getInstigator(), fluidContainer.fluidType, fluidContainer.volume, fluidSlot);
                        event.getWorkstation().send(beforeFluidAdded);
                        if (!beforeFluidAdded.isConsumed()) {
                            return;
                        }
                    }
                }
            }

            for (int slot : WorkstationInventoryUtils.getAssignedSlots(event.getWorkstation(), "FLUID_CONTAINER_OUTPUT")) {
                if (slot == event.getSlotNo()) {
                    if (event.getWorkstation() != event.getInstigator()) {
                        event.consume();
                    }
                    return;
                }
            }

            event.consume();
        }
    }

    /**
     * Check to see if we can use the fluid container item.
     *
     * @param workstation The workstation which interacts with and houses the fluid inventory.
     * @param fluidInventory The component which stores the inventory of fluids.
     * @param containerItem An entity that is a fluid container.
     */
    private boolean canEmptyContainerItem(EntityRef workstation, FluidInventoryComponent fluidInventory, EntityRef containerItem) {
        FluidContainerItemComponent fluidContainer = containerItem.getComponent(FluidContainerItemComponent.class);
        if (fluidContainer != null && fluidContainer.fluidType != null) {
            for (int fluidSlot : WorkstationInventoryUtils.getAssignedSlots(workstation, "FLUID_INPUT")) {
                FluidComponent fluid = fluidInventory.fluidSlots.get(fluidSlot).getComponent(FluidComponent.class);
                Float maximumVolume = fluidInventory.maximumVolumes.get(fluidSlot);

                // If all of the contents of the container can be stored in the fluid inventory.
                if (canStoreContentsOfContainerInFluidSlot(fluidContainer, fluid, maximumVolume)) {
                    EntityRef tempEntity = containerItem.copy();
                    try {
                        FluidContainerItemComponent fluidContainerCopy = tempEntity.getComponent(FluidContainerItemComponent.class);
                        fluidContainerCopy.fluidType = null;
                        for (int containerOutputSlot : WorkstationInventoryUtils.getAssignedSlots(workstation, "FLUID_CONTAINER_OUTPUT")) {
                            EntityRef outputItem = InventoryUtils.getItemAt(workstation, containerOutputSlot);
                            if (InventoryUtils.canStackInto(tempEntity, outputItem)) {
                                return true;
                            }
                        }
                    } finally {
                        tempEntity.destroy();
                    }
                } else if (canPartiallyStoreContentsOfContainerInFluidSlot(fluidContainer, fluid, maximumVolume)) {
                    // If some of the contents of the container can be stored in the fluid inventory.
                    EntityRef tempEntity = containerItem.copy();
                    try {
                        FluidContainerItemComponent fluidContainerCopy = tempEntity.getComponent(FluidContainerItemComponent.class);
                        fluidContainerCopy.volume = Math.max(0f, fluidContainerCopy.volume - (maximumVolume - fluid.volume));

                        if (fluidContainerCopy.volume <= DELTA) {
                            fluidContainerCopy.fluidType = null;
                        }

                        for (int containerOutputSlot : WorkstationInventoryUtils.getAssignedSlots(workstation, "FLUID_CONTAINER_OUTPUT")) {
                            EntityRef outputItem = InventoryUtils.getItemAt(workstation, containerOutputSlot);
                            if (InventoryUtils.canStackInto(tempEntity, outputItem)) {
                                return true;
                            }
                        }
                    } finally {
                        tempEntity.destroy();
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check to see if we can store the entire volume of the fluid container plus pre-existing fluid in this fluid slot.
     *
     * @param fluidContainer The fluid container item component that houses the fluid.
     * @param fluid The fluid component that contains that current level of fluid in whatever.
     * @param maximumVolume The maximum volume of fluid that can be stored.
     */
    private boolean canStoreContentsOfContainerInFluidSlot(FluidContainerItemComponent fluidContainer, FluidComponent fluid, float maximumVolume) {
        return (fluid == null && fluidContainer.volume <= maximumVolume)
                || (fluid != null && fluid.fluidType.equals(fluidContainer.fluidType) && fluidContainer.volume + fluid.volume <= maximumVolume);
    }

    /**
     * Check to see if we can partially store the some of the volume of the fluid container in this fluid slot.
     *
     * @param fluidContainer The fluid container item component that houses the fluid.
     * @param fluid The fluid component that contains that current level of fluid in whatever.
     * @param maximumVolume The maximum volume of fluid that can be stored.
     */
    private boolean canPartiallyStoreContentsOfContainerInFluidSlot(FluidContainerItemComponent fluidContainer, FluidComponent fluid, float maximumVolume) {
        return (fluid == null && fluidContainer.volume <= maximumVolume)
                || (fluid != null && fluid.fluidType.equals(fluidContainer.fluidType) && fluidContainer.volume > 0 && fluid.volume < maximumVolume);
    }
}
