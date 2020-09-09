// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.processPart.inventory;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;

/**
 * Consume this event if the item suggested for the slot is not valid in this process.  This will allow slots on a
 * workstation to only accept items that could be used in a process.
 */
public class ProcessEntityIsInvalidForInventoryItemEvent extends AbstractConsumableEvent {
    EntityRef workstation;
    int slotNo;
    EntityRef instigator;
    EntityRef item;

    public ProcessEntityIsInvalidForInventoryItemEvent(EntityRef workstation, int slotNo, EntityRef instigator,
                                                       EntityRef item) {
        this.workstation = workstation;
        this.slotNo = slotNo;
        this.instigator = instigator;
        this.item = item;
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

    public EntityRef getItem() {
        return item;
    }
}
