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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.fluid.system.FluidManager;
import org.terasology.fluid.system.FluidRegistry;
import org.terasology.fluid.system.FluidUtils;
import org.terasology.registry.In;
import org.terasology.workstation.process.ProcessPartDescription;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.processPart.ProcessEntityFinishExecutionEvent;
import org.terasology.workstation.processPart.ProcessEntityGetDurationEvent;
import org.terasology.workstation.processPart.ProcessEntityIsInvalidEvent;
import org.terasology.workstation.processPart.ProcessEntityIsInvalidToStartEvent;
import org.terasology.workstation.processPart.ProcessEntityStartExecutionEvent;
import org.terasology.workstation.processPart.inventory.ProcessEntityIsInvalidForFluidEvent;
import org.terasology.workstation.processPart.metadata.ProcessEntityGetInputDescriptionEvent;

import java.util.HashMap;
import java.util.Map;

@RegisterSystem
public class FluidInputProcessPartCommonSystem extends BaseComponentSystem {
    public static final String FLUIDINPUTCATEGORY = "FLUIDINPUT";
    private static final Logger logger = LoggerFactory.getLogger(FluidInputProcessPartCommonSystem.class);

    @In
    FluidRegistry fluidRegistry;
    @In
    FluidManager fluidManager;

    ///// Processing

    @ReceiveEvent
    public void validateProcess(ProcessEntityIsInvalidEvent event, EntityRef processEntity,
                                FluidInputComponent fluidInputComponent) {
        try {
            if (fluidInputComponent.fluidVolumes.size() == 0) {
                event.addError("No input fluids specified in " + this.getClass().getSimpleName());
            }

            for (Map.Entry<String, Float> fluidAmount : fluidInputComponent.fluidVolumes.entrySet()) {
                ResourceUrn resourceUrn = new ResourceUrn(fluidAmount.getKey());
                String fluidName = fluidRegistry.getDisplayName(resourceUrn.toString());
                if (fluidName == null) {
                    event.addError(fluidAmount.getKey() + " is an invalid fluid in " + this.getClass().getSimpleName());
                }
            }
        } catch (Exception ex) {
            event.addError("Could not create input fluids in " + this.getClass().getSimpleName());
        }
    }

    @ReceiveEvent
    public void validateToStartExecution(ProcessEntityIsInvalidToStartEvent event, EntityRef processEntity,
                                         FluidInputComponent fluidInputComponent) {
        // Select the items to consume and save it to the process entity using the InventoryInputItemsComponent
        final Map<Integer, Float> slotAmounts = Maps.newHashMap();

        for (Map.Entry<Predicate<String>, Float> requiredItem : getInputItems(fluidInputComponent).entrySet()) {
            Predicate<String> filter = requiredItem.getKey();
            float remainingToFind = requiredItem.getValue();
            boolean foundItem = false;
            for (int slot : WorkstationInventoryUtils.getAssignedInputSlots(event.getWorkstation(), FLUIDINPUTCATEGORY)) {
                String item = FluidUtils.getFluidAt(event.getWorkstation(), slot);
                if (filter.apply(item)) {
                    ProcessEntityIsInvalidForFluidEvent validForFluidEvent = new ProcessEntityIsInvalidForFluidEvent(event.getWorkstation(), slot, event.getInstigator(), item);
                    processEntity.send(validForFluidEvent);
                    if (validForFluidEvent.isConsumed()) {
                        continue;
                    }

                    foundItem = true;
                    float amountToUse = Math.min(remainingToFind, FluidUtils.getFluidAmount(event.getWorkstation(), slot));
                    slotAmounts.put(slot, amountToUse);
                    remainingToFind -= amountToUse;
                    if (remainingToFind == 0) {
                        break;
                    }
                }
            }

            if (!foundItem || remainingToFind > 0) {
                event.consume();
                return;
            }

        }

        FluidInputProcessPartSlotAmountsComponent slotAmountsComponent = processEntity.getComponent(FluidInputProcessPartSlotAmountsComponent.class);
        if (slotAmountsComponent == null) {
            processEntity.addComponent(new FluidInputProcessPartSlotAmountsComponent(slotAmounts));
        } else {
            slotAmountsComponent.slotAmounts.putAll(slotAmounts);
            processEntity.saveComponent(slotAmountsComponent);
        }
    }

    @ReceiveEvent
    public void startExecution(ProcessEntityStartExecutionEvent event, EntityRef processEntity,
                               FluidInputComponent fluidInputComponent) {
        FluidInputProcessPartSlotAmountsComponent inputItems = processEntity.getComponent(FluidInputProcessPartSlotAmountsComponent.class);
        // this will be null if another process part has already consumed the items
        if (inputItems != null) {
            for (Map.Entry<Integer, Float> slotAmount : inputItems.slotAmounts.entrySet()) {
                String fluid = FluidUtils.getFluidAt(event.getWorkstation(), slotAmount.getKey());
                if (slotAmount.getValue() > FluidUtils.getFluidAmount(event.getWorkstation(), slotAmount.getKey())) {
                    logger.error("Not enough fluid in the slot");
                }
                if (!fluidManager.removeFluid(event.getInstigator(), event.getWorkstation(), slotAmount.getKey(), fluid, slotAmount.getValue())) {
                    logger.error("Could not remove input fluid");
                }
            }
        }

        // remove the slot amounts from the process entity, no other InventoryInput should use it
        processEntity.removeComponent(FluidInputProcessPartSlotAmountsComponent.class);
    }

    @ReceiveEvent
    public void getDuration(ProcessEntityGetDurationEvent event, EntityRef processEntity,
                            FluidInputComponent fluidInputComponent) {
    }

    @ReceiveEvent
    public void finishExecution(ProcessEntityFinishExecutionEvent event, EntityRef entityRef,
                                FluidInputComponent fluidInputComponent) {
    }

    ///// Fluid

    @ReceiveEvent
    public void isValidFluid(ProcessEntityIsInvalidForFluidEvent event, EntityRef processEntity,
                             FluidInputComponent fluidInputComponent) {
        if (WorkstationInventoryUtils.getAssignedInputSlots(event.getWorkstation(), FLUIDINPUTCATEGORY).contains(event.getSlotNo())) {
            if (!Iterables.any(getInputItems(fluidInputComponent).keySet(), x -> x.apply(event.getFluidType()))) {
                event.consume();
            }
        }
    }


    ///// Metadata

    @ReceiveEvent
    public void getInputDescriptions(ProcessEntityGetInputDescriptionEvent event, EntityRef processEntity,
                                     FluidInputComponent fluidInputComponent) {
        for (Map.Entry<String, Float> fluidAmount : fluidInputComponent.fluidVolumes.entrySet()) {
            String fluidName = fluidRegistry.getDisplayName(fluidAmount.getKey());
            event.addInputDescription(new ProcessPartDescription(new ResourceUrn(fluidAmount.getKey()), fluidAmount.getValue() + "mL " + fluidName));
        }
    }

    private Map<Predicate<String>, Float> getInputItems(FluidInputComponent fluidInputComponent) {
        Map<Predicate<String>, Float> result = new HashMap<>();
        for (final Map.Entry<String, Float> fluidAmount : fluidInputComponent.fluidVolumes.entrySet()) {
            result.put(new Predicate<String>() {
                @Override
                public boolean apply(String input) {
                    return fluidAmount.getKey().equals(input);
                }
            }, fluidAmount.getValue());
        }

        return result;
    }
}
