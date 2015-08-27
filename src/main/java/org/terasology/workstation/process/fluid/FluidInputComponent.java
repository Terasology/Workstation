/*
 * Copyright 2015 MovingBlocks
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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.fluid.system.FluidManager;
import org.terasology.fluid.system.FluidRegistry;
import org.terasology.fluid.system.FluidRenderer;
import org.terasology.fluid.system.FluidUtils;
import org.terasology.registry.CoreRegistry;
import org.terasology.workstation.process.DescribeProcess;
import org.terasology.workstation.process.ErrorCheckingProcessPart;
import org.terasology.workstation.process.InvalidProcessPartException;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.ProcessPartDescription;
import org.terasology.workstation.process.ProcessPartOrdering;
import org.terasology.workstation.process.WorkstationInventoryUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FluidInputComponent implements Component, ProcessPart, ValidateFluidInventoryItem, DescribeProcess, ErrorCheckingProcessPart, ProcessPartOrdering {
    public static final int SORTORDER = -1;
    public static final String FLUIDINPUTCATEGORY = "FLUIDINPUT";
    private static final Logger logger = LoggerFactory.getLogger(FluidInputComponent.class);

    public Map<String, Float> fluidVolumes = Maps.newHashMap();

    protected Map<Predicate<String>, Float> getInputItems() {
        Map<Predicate<String>, Float> result = new HashMap<>();
        for (final Map.Entry<String, Float> fluidAmount : fluidVolumes.entrySet()) {
            result.put(new Predicate<String>() {
                @Override
                public boolean apply(String input) {
                    return fluidAmount.getKey().equals(input);
                }
            }, fluidAmount.getValue());
        }

        return result;
    }

    @Override
    public boolean isResponsibleForFluidSlot(EntityRef workstation, int slotNo) {
        return WorkstationInventoryUtils.getAssignedInputSlots(workstation, FLUIDINPUTCATEGORY).contains(slotNo);
    }

    @Override
    public boolean isValidFluid(EntityRef workstation, int slotNo, EntityRef instigator, String fluidType) {
        for (Predicate<String> inputPredicate : getInputItems().keySet()) {
            if (inputPredicate.apply(fluidType)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean validateBeforeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        // Select the items to consume and save it to the process entity using the InventoryInputProcessPartSlotAmountsComponent
        final Map<Integer, Float> slotAmounts = Maps.newHashMap();

        for (Map.Entry<Predicate<String>, Float> requiredItem : getInputItems().entrySet()) {
            Predicate<String> filter = requiredItem.getKey();
            float remainingToFind = requiredItem.getValue();
            boolean foundItem = false;
            for (int slot : WorkstationInventoryUtils.getAssignedInputSlots(workstation, FLUIDINPUTCATEGORY)) {
                String item = FluidUtils.getFluidAt(workstation, slot);
                if (filter.apply(item)) {
                    foundItem = true;
                    float amountToUse = Math.min(remainingToFind, FluidUtils.getFluidAmount(workstation, slot));
                    slotAmounts.put(slot, amountToUse);
                    remainingToFind -= amountToUse;
                    if (remainingToFind == 0) {
                        break;
                    }
                }
            }

            if (!foundItem || remainingToFind > 0) {
                return false;
            }

        }

        FluidInputProcessPartSlotAmountsComponent slotAmountsComponent = processEntity.getComponent(FluidInputProcessPartSlotAmountsComponent.class);
        if (slotAmountsComponent == null) {
            processEntity.addComponent(new FluidInputProcessPartSlotAmountsComponent(slotAmounts));
        } else {
            slotAmountsComponent.slotAmounts.putAll(slotAmounts);
            processEntity.saveComponent(slotAmountsComponent);
        }

        return true;
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        return 0;
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        FluidInputProcessPartSlotAmountsComponent inputItems = processEntity.getComponent(FluidInputProcessPartSlotAmountsComponent.class);
        // this will be null if another process part has already consumed the items
        if (inputItems != null) {
            for (Map.Entry<Integer, Float> slotAmount : inputItems.slotAmounts.entrySet()) {
                String fluid = FluidUtils.getFluidAt(workstation, slotAmount.getKey());
                if (slotAmount.getValue() > FluidUtils.getFluidAmount(workstation, slotAmount.getKey())) {
                    logger.error("Not enough fluid in the slot");
                }
                if (!CoreRegistry.get(FluidManager.class).removeFluid(instigator, workstation, slotAmount.getKey(), fluid, slotAmount.getValue())) {
                    logger.error("Could not remove input fluid");
                }
            }
        }

        // remove the slot amounts from the process entity, no other InventoryInput should use it
        processEntity.removeComponent(FluidInputProcessPartSlotAmountsComponent.class);
    }

    @Override
    public int getSortOrder() {
        return SORTORDER;
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
    }

    @Override
    public Collection<ProcessPartDescription> getInputDescriptions() {
        FluidRegistry fluidRegistry = CoreRegistry.get(FluidRegistry.class);
        Set<ProcessPartDescription> descriptions = Sets.newHashSet();

        for (Map.Entry<String, Float> fluidAmount : fluidVolumes.entrySet()) {
            String fluidName = fluidAmount.getKey();
            FluidRenderer fluidRenderer = fluidRegistry.getFluidRenderer(fluidAmount.getKey());
            if (fluidRenderer == null) {
                fluidName = fluidRenderer.getFluidName();
            }
            descriptions.add(new ProcessPartDescription(new ResourceUrn(fluidAmount.getKey()), fluidAmount.getValue() + "mL " + fluidName));
        }

        return descriptions;
    }

    @Override
    public Collection<ProcessPartDescription> getOutputDescriptions() {
        return Collections.emptyList();
    }

    @Override
    public void checkForErrors() throws InvalidProcessPartException {
        FluidRegistry fluidRegistry = CoreRegistry.get(FluidRegistry.class);
        try {
            if (fluidVolumes.size() == 0) {
                throw new InvalidProcessPartException("No input fluids specified in " + this.getClass().getSimpleName());
            }

            for (Map.Entry<String, Float> fluidAmount : fluidVolumes.entrySet()) {
                ResourceUrn resourceUrn = new ResourceUrn(fluidAmount.getKey());
                FluidRenderer fluidRenderer = fluidRegistry.getFluidRenderer(resourceUrn.toString());
                if (fluidRenderer == null) {
                    throw new InvalidProcessPartException(fluidAmount.getKey() + " is an invalid fluid in " + this.getClass().getSimpleName());
                }
            }
        } catch (Exception ex) {
            throw new InvalidProcessPartException("Could not create input fluids in " + this.getClass().getSimpleName());
        }
    }
}
