// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.ServerEvent;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * Send this event to the instigating entity (like the player character)
 */
@ServerEvent
public class WorkstationProcessRequest implements Event {
    private EntityRef workstation;
    private String processId;

    public WorkstationProcessRequest() {
    }

    public WorkstationProcessRequest(EntityRef workstation, String processId) {
        this.workstation = workstation;
        this.processId = processId;
    }

    public EntityRef getWorkstation() {
        return workstation;
    }

    public String getProcessId() {
        return processId;
    }
}
