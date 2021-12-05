// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.system;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.fluid.component.FluidInventoryComponent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.module.inventory.events.InventorySlotChangedEvent;
import org.terasology.module.inventory.events.InventorySlotStackSizeChangedEvent;
import org.terasology.workstation.component.WorkstationComponent;
import org.terasology.workstation.event.WorkstationStateChanged;

@RegisterSystem(RegisterMode.AUTHORITY)
public class InventoryBasedAutomaticProcessingSystem extends BaseComponentSystem {
    @ReceiveEvent
    public void newItemInWorkstation(InventorySlotChangedEvent event, EntityRef workstation, WorkstationComponent workstationComponent) {
        workstation.send(new WorkstationStateChanged());
    }

    @ReceiveEvent
    public void itemCountChangedInWorkstation(InventorySlotStackSizeChangedEvent event, EntityRef workstation, WorkstationComponent workstationComponent) {
        workstation.send(new WorkstationStateChanged());
    }

    @ReceiveEvent
    public void fluidInventoryChangedInWorkstation(OnChangedComponent event, EntityRef workstation, WorkstationComponent workstationComponent, FluidInventoryComponent fluidInventoryComponent) {
        workstation.send(new WorkstationStateChanged());
    }
}
