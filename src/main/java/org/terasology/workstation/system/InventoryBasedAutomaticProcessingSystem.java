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
package org.terasology.workstation.system;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.inventory.events.InventorySlotStackSizeChangedEvent;
import org.terasology.fluid.component.FluidInventoryComponent;
import org.terasology.workstation.component.WorkstationComponent;
import org.terasology.workstation.event.WorkstationStateChanged;

@RegisterSystem(RegisterMode.AUTHORITY)
public class InventoryBasedAutomaticProcessingSystem extends BaseComponentSystem {
    @ReceiveEvent
    public void newItemInWorkstation(InventorySlotChangedEvent event, EntityRef workstation, WorkstationComponent workstationComponent) {
        workstation.send(new WorkstationStateChanged());
    }

    @ReceiveEvent
    public void itemCountChangedInWorkstation(InventorySlotStackSizeChangedEvent event, EntityRef workstation, WorkstationComponent workstationComponent) {
        workstation.send(new WorkstationStateChanged());
    }

    @ReceiveEvent
    public void fluidInventoryChangedInWorkstation(OnChangedComponent event, EntityRef workstation, WorkstationComponent workstationComponent, FluidInventoryComponent fluidInventoryComponent) {
        workstation.send(new WorkstationStateChanged());
    }
}
