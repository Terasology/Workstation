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
