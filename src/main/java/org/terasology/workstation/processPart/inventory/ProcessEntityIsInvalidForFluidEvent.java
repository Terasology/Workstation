// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.processPart.inventory;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;

/**
 * Consume this event if the fluid suggested for the slot is not valid in this process.  This will allow slots on a
 * workstation to only accept items that could be used in a process.
 */
public class ProcessEntityIsInvalidForFluidEvent extends AbstractConsumableEvent {
    EntityRef workstation;
    int slotNo;
    EntityRef instigator;
    String fluidType;

    public ProcessEntityIsInvalidForFluidEvent(EntityRef workstation, int slotNo, EntityRef instigator,
                                               String fluidType) {
        this.workstation = workstation;
        this.slotNo = slotNo;
        this.instigator = instigator;
        this.fluidType = fluidType;
    }

    public EntityRef getWorkstation() {
        return workstation;
    }

    public int getSlotNo() {
        return slotNo;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public String getFluidType() {
        return fluidType;
    }
}
