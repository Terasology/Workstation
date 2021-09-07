/*
 * Copyright 2016 MovingBlocks
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
