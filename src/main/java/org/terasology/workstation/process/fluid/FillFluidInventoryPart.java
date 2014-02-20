package org.terasology.workstation.process.fluid;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.fluid.component.FluidComponent;
import org.terasology.fluid.component.FluidContainerItemComponent;
import org.terasology.fluid.component.FluidInventoryComponent;
import org.terasology.fluid.event.BeforeFluidPutInInventory;
import org.terasology.fluid.system.FluidManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.action.GiveItemAction;
import org.terasology.logic.inventory.action.RemoveItemAction;
import org.terasology.registry.CoreRegistry;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.process.inventory.ValidateInventoryItem;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class FillFluidInventoryPart implements Component, ProcessPart, ValidateInventoryItem {
    @Override
    public boolean isResponsibleForSlot(EntityRef workstation, int slotNo) {
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "FLUID_CONTAINER_INPUT")) {
            if (slot == slotNo) {
                return true;
            }
        }

        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "FLUID_CONTAINER_OUTPUT")) {
            if (slot == slotNo) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isValid(EntityRef workstation, int slotNo, EntityRef instigator, EntityRef item) {
        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "FLUID_CONTAINER_INPUT")) {
            if (slot == slotNo) {
                FluidContainerItemComponent fluidContainer = item.getComponent(FluidContainerItemComponent.class);
                if (fluidContainer == null) {
                    return false;
                }
                if (fluidContainer.fluidType == null) {
                    return false;
                }

                FluidInventoryComponent fluidInventory = workstation.getComponent(FluidInventoryComponent.class);
                if (fluidInventory == null) {
                    return false;
                }

                for (int fluidSlot : WorkstationInventoryUtils.getAssignedSlots(workstation, "FLUID_INPUT")) {
                    BeforeFluidPutInInventory beforeFluidAdded = new BeforeFluidPutInInventory(instigator, fluidContainer.fluidType, fluidContainer.volume, fluidSlot);
                    workstation.send(beforeFluidAdded);
                    if (!beforeFluidAdded.isConsumed()) {
                        return true;
                    }
                }
            }
        }

        for (int slot : WorkstationInventoryUtils.getAssignedSlots(workstation, "FLUID_CONTAINER_OUTPUT")) {
            if (slot == slotNo) {
                return workstation == instigator;
            }
        }

        return false;
    }

    @Override
    public Set<String> validate(EntityRef instigator, EntityRef workstation, String parameter) throws InvalidProcessException {
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);

        FluidInventoryComponent fluidInventory = workstation.getComponent(FluidInventoryComponent.class);

        Set<String> result = new HashSet<>();
        for (int containerInputSlot : WorkstationInventoryUtils.getAssignedSlots(workstation, "FLUID_CONTAINER_INPUT")) {
            EntityRef containerItem = InventoryUtils.getItemAt(workstation, containerInputSlot);
            if (canEmptyContainerItem(workstation, entityManager, fluidInventory, containerItem)) {
                result.add(String.valueOf(containerInputSlot));
            }
        }

        if (result.size() == 0) {
            throw new InvalidProcessException();
        }

        return result;
    }

    private boolean canEmptyContainerItem(EntityRef workstation, EntityManager entityManager, FluidInventoryComponent fluidInventory, EntityRef containerItem) {
        FluidContainerItemComponent fluidContainer = containerItem.getComponent(FluidContainerItemComponent.class);
        if (fluidContainer != null && fluidContainer.fluidType != null) {
            for (int fluidSlot : WorkstationInventoryUtils.getAssignedSlots(workstation, "FLUID_INPUT")) {
                FluidComponent fluid = fluidInventory.fluidSlots.get(fluidSlot).getComponent(FluidComponent.class);
                Float maximumVolume = fluidInventory.maximumVolumes.get(fluidSlot);
                if (canStoreContentsOfContainerInFluidSlot(fluidContainer, fluid, maximumVolume)) {
                    EntityRef tempEntity = entityManager.copy(containerItem);
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
            }
        }
        return false;
    }

    private boolean canStoreContentsOfContainerInFluidSlot(FluidContainerItemComponent fluidContainer, FluidComponent fluid, float maximumVolume) {
        return (fluid == null && fluidContainer.volume <= maximumVolume)
                || (fluid != null && fluid.fluidType.equals(fluidContainer.fluidType) && fluidContainer.volume + fluid.volume <= maximumVolume);
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, String result, String parameter) {
        return 0;
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, String result, String parameter) {
        FluidManager fluidManager = CoreRegistry.get(FluidManager.class);

        int slot = Integer.parseInt(result);
        EntityRef containerItem = InventoryUtils.getItemAt(workstation, slot);
        FluidContainerItemComponent fluidContainer = containerItem.getComponent(FluidContainerItemComponent.class);

        for (int fluidSlot : WorkstationInventoryUtils.getAssignedSlots(workstation, "FLUID_INPUT")) {
            if (fluidManager.addFluid(instigator, workstation, fluidSlot, fluidContainer.fluidType, fluidContainer.volume)) {
                break;
            }
        }

        RemoveItemAction remove = new RemoveItemAction(instigator, containerItem, false, 1);
        workstation.send(remove);
        if (remove.isConsumed()) {
            EntityRef removedItem = remove.getRemovedItem();
            FluidContainerItemComponent fluidContainerRemoved = removedItem.getComponent(FluidContainerItemComponent.class);
            fluidContainerRemoved.fluidType = null;
            removedItem.saveComponent(fluidContainerRemoved);

            for (int containerOutputSlot : WorkstationInventoryUtils.getAssignedSlots(workstation, "FLUID_CONTAINER_OUTPUT")) {
                GiveItemAction give = new GiveItemAction(instigator, removedItem, containerOutputSlot);
                workstation.send(give);
                if (give.isConsumed()) {
                    return;
                }
            }

            removedItem.destroy();
        }
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, String result, String parameter) {
    }
}
