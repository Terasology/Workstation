/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.workstation.system;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.fluid.component.FluidInventoryComponent;
import org.terasology.fluid.event.BeforeFluidPutInInventory;
import org.terasology.registry.In;
import org.terasology.workstation.component.WorkstationComponent;
import org.terasology.workstation.component.WorkstationProcessType;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.process.fluid.ValidateFluidInventoryItem;

@RegisterSystem
public class WorkstationFluidInventoryValidationSystem extends BaseComponentSystem {
    @In
    private WorkstationRegistry workstationRegistry;

    @ReceiveEvent
    public void itemPutIntoWorkstation(BeforeFluidPutInInventory event, EntityRef entity,
                                       WorkstationComponent workstation, FluidInventoryComponent fluidInventory) {
        int slot = event.getSlot();

        boolean hasValidation = false;
        for (WorkstationProcess workstationProcess : workstationRegistry.getWorkstationProcesses(workstation.supportedProcessTypes.keySet())) {
            if (workstationProcess instanceof ValidateFluidInventoryItem) {
                ValidateFluidInventoryItem inventoryValidator = (ValidateFluidInventoryItem) workstationProcess;
                hasValidation = true;
                if (inventoryValidator.isValidFluid(entity, slot, event.getInstigator(), event.getFluidType())) {
                    return;
                }
            }
        }

        if (hasValidation) {
            // There were validators, but no process has accepted this item
            event.consume();
        }
    }
}
