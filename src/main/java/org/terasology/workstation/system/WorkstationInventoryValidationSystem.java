// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.system;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.inventory.logic.InventoryComponent;
import org.terasology.inventory.logic.events.BeforeItemPutInInventory;
import org.terasology.workstation.component.WorkstationComponent;
import org.terasology.workstation.process.WorkstationProcess;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class WorkstationInventoryValidationSystem extends BaseComponentSystem {
    @In
    private WorkstationRegistry workstationRegistry;

    @ReceiveEvent
    public void itemPutIntoWorkstation(BeforeItemPutInInventory event, EntityRef entity,
                                       WorkstationComponent workstation, InventoryComponent workstationInventory) {
        int slot = event.getSlot();

        boolean hasValidation = false;
        for (WorkstationProcess workstationProcess :
                workstationRegistry.getWorkstationProcesses(workstation.supportedProcessTypes.keySet())) {
            if (workstationProcess instanceof ValidateInventoryItem) {
                ValidateInventoryItem inventoryValidator = (ValidateInventoryItem) workstationProcess;
                hasValidation = true;
                if (inventoryValidator.isValid(entity, slot, event.getInstigator(), event.getItem())) {
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
