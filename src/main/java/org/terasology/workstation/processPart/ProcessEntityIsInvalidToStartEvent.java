// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.processPart;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;

/**
 * Consuming this event signals that this process entity is invalid and prevents any further validation
 */
public class ProcessEntityIsInvalidToStartEvent extends AbstractConsumableEvent {
    EntityRef instigator;
    EntityRef workstation;

    public ProcessEntityIsInvalidToStartEvent(EntityRef instigator, EntityRef workstation) {
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
