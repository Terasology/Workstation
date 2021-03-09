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

import com.google.common.collect.Maps;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.fluid.system.FluidManager;
import org.terasology.fluid.system.FluidRegistry;
import org.terasology.fluid.system.FluidUtils;
import org.terasology.workstation.process.ProcessPartDescription;
import org.terasology.workstation.process.WorkstationInventoryUtils;
import org.terasology.workstation.processPart.ProcessEntityFinishExecutionEvent;
import org.terasology.workstation.processPart.ProcessEntityIsInvalidEvent;
import org.terasology.workstation.processPart.ProcessEntityIsInvalidToStartEvent;
import org.terasology.workstation.processPart.inventory.ProcessEntityIsInvalidForFluidEvent;
import org.terasology.workstation.processPart.metadata.ProcessEntityGetOutputDescriptionEvent;

import java.util.Map;

@RegisterSystem
public class FluidOutputProcessPartCommonSystem extends BaseComponentSystem {
    public static final String FLUIDOUTPUTCATEGORY = "FLUIDOUTPUT";

    @In
    FluidManager fluidManager;
    @In
    FluidRegistry fluidRegistry;

    ///// Processing

    @ReceiveEvent
    public void validateProcess(ProcessEntityIsInvalidEvent event, EntityRef processEntity,
                                FluidOutputComponent fluidOutputComponent) {
        try {
            if (fluidOutputComponent.fluidVolumes.size() == 0) {
                event.addError("No output fluids specified in " + this.getClass().getSimpleName());
            }

            for (Map.Entry<String, Float> fluidAmount : fluidOutputComponent.fluidVolumes.entrySet()) {
                ResourceUrn resourceUrn = new ResourceUrn(fluidAmount.getKey());
                String fluidName = fluidRegistry.getDisplayName(resourceUrn.toString());
                if (fluidName == null) {
                    event.addError(fluidAmount.getKey() + " is an invalid fluid in " + this.getClass().getSimpleName());
                }
            }
        } catch (Exception ex) {
            event.addError("Could not create output fluids in " + this.getClass().getSimpleName());
        }
    }

    @ReceiveEvent
    public void validateToStartExecution(ProcessEntityIsInvalidToStartEvent event, EntityRef processEntity,
                                         FluidOutputComponent fluidOutputComponent) {
        Map<String, Float> outputItems = Maps.newHashMap(fluidOutputComponent.fluidVolumes);

        Map<String, Float> itemsLeftToAssign = Maps.newHashMap(outputItems);
        int emptySlots = 0;

        for (int slot : WorkstationInventoryUtils.getAssignedOutputSlots(event.getWorkstation(), FLUIDOUTPUTCATEGORY)) {
            String fluid = FluidUtils.getFluidAt(event.getWorkstation(), slot);
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
            event.consume();
        }
    }

    @ReceiveEvent
    public void finishExecution(ProcessEntityFinishExecutionEvent event, EntityRef entityRef,
                                FluidOutputComponent fluidOutputComponent) {
        Map<String, Float> outputItems = Maps.newHashMap(fluidOutputComponent.fluidVolumes);

        for (Map.Entry<String, Float> outputItem : outputItems.entrySet()) {
            for (int slot : WorkstationInventoryUtils.getAssignedOutputSlots(event.getWorkstation(), FLUIDOUTPUTCATEGORY)) {
                if (fluidManager.addFluid(event.getInstigator(), event.getWorkstation(), slot, outputItem.getKey(), outputItem.getValue())) {
                    break;
                }
            }
        }
    }

    ///// Fluid

    @ReceiveEvent
    public void blockFamilyInputIsValidFluid(ProcessEntityIsInvalidForFluidEvent event, EntityRef processEntity,
                                             FluidOutputComponent fluidOutputComponent) {
        if (WorkstationInventoryUtils.getAssignedOutputSlots(event.getWorkstation(), FLUIDOUTPUTCATEGORY).contains(event.getSlotNo())) {
            if (!event.getWorkstation().equals(event.getInstigator())) {
                event.consume();
            }
        }
    }


    ///// Metadata

    @ReceiveEvent
    public void getOutputDescriptions(ProcessEntityGetOutputDescriptionEvent event, EntityRef processEntity,
                                      FluidOutputComponent fluidOutputComponent) {
        for (Map.Entry<String, Float> fluidAmount : fluidOutputComponent.fluidVolumes.entrySet()) {
            String fluidName = fluidRegistry.getDisplayName(fluidAmount.getKey());
            event.addOutputDescription(new ProcessPartDescription(new ResourceUrn(fluidAmount.getKey()), fluidAmount.getValue() + "mL " + fluidName));
        }
    }
}
