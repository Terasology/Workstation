// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.system;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.fluid.component.FluidInventoryComponent;
import org.terasology.fluid.event.BeforeFluidPutInInventory;
import org.terasology.workstation.component.WorkstationComponent;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.process.fluid.ValidateFluidInventoryItem;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class WorkstationFluidInventoryValidationSystem extends BaseComponentSystem {
    @In
    private WorkstationRegistry workstationRegistry;

    @ReceiveEvent
    public void itemPutIntoWorkstation(BeforeFluidPutInInventory event, EntityRef entity,
                                       WorkstationComponent workstation, FluidInventoryComponent fluidInventory) {
        int slot = event.getSlot();

        boolean hasValidation = false;
        for (WorkstationProcess workstationProcess :
                workstationRegistry.getWorkstationProcesses(workstation.supportedProcessTypes.keySet())) {
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
