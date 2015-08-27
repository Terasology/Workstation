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
import java.util.Map;
import java.util.Set;

public class FluidOutputComponent implements Component, ProcessPart, ValidateFluidInventoryItem, DescribeProcess, ErrorCheckingProcessPart, ProcessPartOrdering {
    public static final int SORTORDER = 1;
    public static final String FLUIDOUTPUTCATEGORY = "FLUIDOUTPUT";
    private static final Logger logger = LoggerFactory.getLogger(FluidOutputComponent.class);

    public Map<String, Float> fluidVolumes = Maps.newHashMap();

    @Override
    public boolean isResponsibleForFluidSlot(EntityRef workstation, int slotNo) {
        return WorkstationInventoryUtils.getAssignedOutputSlots(workstation, FLUIDOUTPUTCATEGORY).contains(slotNo);
    }

    @Override
    public boolean isValidFluid(EntityRef workstation, int slotNo, EntityRef instigator, String fluidType) {
        return workstation == instigator;
    }

    @Override
    public boolean validateBeforeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        Map<String, Float> outputItems = Maps.newHashMap(fluidVolumes);

        Map<String, Float> itemsLeftToAssign = Maps.newHashMap(outputItems);
        int emptySlots = 0;

        for (int slot : WorkstationInventoryUtils.getAssignedOutputSlots(workstation, FLUIDOUTPUTCATEGORY)) {
            String fluid = FluidUtils.getFluidAt(workstation, slot);
            if (fluid != null) {
                for (Map.Entry<String, Float> itemLeftToAssign : itemsLeftToAssign.entrySet()) {
                    if (itemLeftToAssign.getKey().equals(fluid)) {
                        itemsLeftToAssign.remove(fluid);
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

        return true;
    }

    @Override
    public long getDuration(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        return 0;
    }

    @Override
    public void executeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
    }

    @Override
    public void executeEnd(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        Map<String, Float> outputItems = Maps.newHashMap(fluidVolumes);

        for (Map.Entry<String, Float> outputItem : outputItems.entrySet()) {
            for (int slot : WorkstationInventoryUtils.getAssignedOutputSlots(workstation, FLUIDOUTPUTCATEGORY)) {
                if (CoreRegistry.get(FluidManager.class).addFluid(instigator, workstation, slot, outputItem.getKey(), outputItem.getValue())) {
                    break;
                }
            }
        }
    }

    @Override
    public Collection<ProcessPartDescription> getInputDescriptions() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ProcessPartDescription> getOutputDescriptions() {
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
    public void checkForErrors() throws InvalidProcessPartException {
        FluidRegistry fluidRegistry = CoreRegistry.get(FluidRegistry.class);
        try {
            if (fluidVolumes.size() == 0) {
                throw new InvalidProcessPartException("No output fluids specified in " + this.getClass().getSimpleName());
            }

            for (Map.Entry<String, Float> fluidAmount : fluidVolumes.entrySet()) {
                ResourceUrn resourceUrn = new ResourceUrn(fluidAmount.getKey());
                FluidRenderer fluidRenderer = fluidRegistry.getFluidRenderer(resourceUrn.toString());
                if (fluidRenderer == null) {
                    throw new InvalidProcessPartException(fluidAmount.getKey() + " is an invalid fluid in " + this.getClass().getSimpleName());
                }
            }
        } catch (Exception ex) {
            throw new InvalidProcessPartException("Could not create output fluids in " + this.getClass().getSimpleName());
        }
    }

    @Override
    public int getSortOrder() {
        return SORTORDER;
    }
}
