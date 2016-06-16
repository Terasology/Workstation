/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.workstation.process.fluid;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.fluid.component.FluidComponent;
import org.terasology.fluid.component.FluidContainerItemComponent;
import org.terasology.fluid.component.FluidInventoryComponent;
import org.terasology.fluid.event.BeforeFluidPutInInventory;
import org.terasology.fluid.system.FluidManager;
import org.terasology.fluid.system.FluidUtils;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.workstation.component.SpecificInputSlotComponent;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.processPart.ProcessEntityIsInvalidToStartEvent;
import org.terasology.workstation.processPart.ProcessEntityStartExecutionEvent;
import org.terasology.workstation.processPart.inventory.ProcessEntityIsInvalidForInventoryItemEvent;

@RegisterSystem
public class FillFluidInventoryPartProcessPartCommonSystem extends BaseComponentSystem {
    @In
    EntityManager entityManager;
    @In
    FluidManager fluidManager;

    public static final float DELTA = 0.001f;

    ///// Processing

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

            if (CoreRegistry.get(InventoryManager.class).giveItem(event.getWorkstation(), event.getInstigator(), removedItem,
                    WorkstationInventoryUtils.getAssignedSlots(event.getWorkstation(), "FLUID_CONTAINER_OUTPUT"))) {
                return;
            }

            removedItem.destroy();
        }
    }

    ///// Inventory

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
                }
                // If some of the contents of the container can be stored in the fluid inventory.
                else if (canPartiallyStoreContentsOfContainerInFluidSlot(fluidContainer, fluid, maximumVolume)) {
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

    // Check to see if we can store the entire volume of the fluid container plus pre-existing fluid in this fluid slot.
    private boolean canStoreContentsOfContainerInFluidSlot(FluidContainerItemComponent fluidContainer, FluidComponent fluid, float maximumVolume) {
        return (fluid == null && fluidContainer.volume <= maximumVolume)
                || (fluid != null && fluid.fluidType.equals(fluidContainer.fluidType) && fluidContainer.volume + fluid.volume <= maximumVolume);
    }

    // Check to see if we can partially store the some of the volume of the fluid container in this fluid slot.
    private boolean canPartiallyStoreContentsOfContainerInFluidSlot(FluidContainerItemComponent fluidContainer, FluidComponent fluid, float maximumVolume) {
        return (fluid == null && fluidContainer.volume <= maximumVolume)
                || (fluid != null && fluid.fluidType.equals(fluidContainer.fluidType) && fluidContainer.volume > 0 && fluid.volume < maximumVolume);
    }
}
