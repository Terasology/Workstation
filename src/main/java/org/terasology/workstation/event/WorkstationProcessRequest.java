/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.workstation.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.ServerEvent;

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
