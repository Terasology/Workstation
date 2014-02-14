package org.terasology.workstation.system;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.events.BeforeItemPutInInventory;
import org.terasology.registry.In;
import org.terasology.workstation.component.WorkstationComponent;
import org.terasology.workstation.component.WorkstationInventoryComponent;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.process.inventory.ValidateInventoryItem;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class WorkstationInventoryValidationSystem extends BaseComponentSystem {
    @In
    private WorkstationRegistry workstationRegistry;

    @ReceiveEvent
    public void itemPutIntoWorkstation(BeforeItemPutInInventory event, EntityRef entity,
                                       WorkstationComponent workstation, WorkstationInventoryComponent workstationInventory) {
        int slot = event.getSlot();

        boolean hasValidation = false;
        for (WorkstationProcess workstationProcess : workstationRegistry.getWorkstationProcesses(workstation.supportedProcessTypes)) {
            for (ProcessPart processPart : workstationProcess.getProcessParts()) {
                if (processPart instanceof ValidateInventoryItem) {
                    ValidateInventoryItem inventoryValidator = (ValidateInventoryItem) processPart;
                    if (inventoryValidator.isResponsibleForSlot(entity, slot)) {
                        hasValidation = true;
                        if (inventoryValidator.isValid(entity, slot, event.getInstigator(), event.getItem())) {
                            return;
                        }
                    }
                }
            }
        }

        if (hasValidation) {
            // There were validators, but no process has accepted this item
            event.consume();
        }
    }
}
