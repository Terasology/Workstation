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
package org.terasology.workstation.processPart.inventory;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.AbstractConsumableEvent;

/**
 * Consume this event if the fluid suggested for the slot is not valid in this process.  This will allow slots on a workstation to only accept items that could be used in a process.
 */
public class ProcessEntityIsInvalidForFluidEvent extends AbstractConsumableEvent {
    EntityRef workstation;
    int slotNo;
    EntityRef instigator;
    String fluidType;

    public ProcessEntityIsInvalidForFluidEvent(EntityRef workstation, int slotNo, EntityRef instigator, String fluidType) {
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
