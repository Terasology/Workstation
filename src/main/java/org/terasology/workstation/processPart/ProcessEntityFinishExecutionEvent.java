// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.processPart;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

/**
 * This event is fired on the process entity at the very end, and possibly after a delay.
 */
public class ProcessEntityFinishExecutionEvent implements Event {
    EntityRef instigator;
    EntityRef workstation;

    public ProcessEntityFinishExecutionEvent(EntityRef instigator, EntityRef workstation) {
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
