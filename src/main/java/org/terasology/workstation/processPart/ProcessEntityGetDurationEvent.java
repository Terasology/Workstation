// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.processPart;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractValueModifiableEvent;

/**
 * Handle this event to modify how long a process takes based on the components on the process entity.
 */
public class ProcessEntityGetDurationEvent extends AbstractValueModifiableEvent {
    EntityRef workstation;
    EntityRef instigator;

    public ProcessEntityGetDurationEvent(float baseValue, EntityRef workstation, EntityRef instigator) {
        super(baseValue);
        this.workstation = workstation;
        this.instigator = instigator;
    }

    public EntityRef getWorkstation() {
        return workstation;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
