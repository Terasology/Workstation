// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.processPart;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * This event is fired on the process entity at the very start after validation.
 */
public class ProcessEntityStartExecutionEvent implements Event {
    EntityRef instigator;
    EntityRef workstation;

    public ProcessEntityStartExecutionEvent(EntityRef instigator, EntityRef workstation) {
        this.instigator = instigator;
        this.workstation = workstation;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public EntityRef getWorkstation() {
        return workstation;
    }
}
