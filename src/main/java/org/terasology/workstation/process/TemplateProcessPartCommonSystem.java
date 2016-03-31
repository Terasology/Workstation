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
package org.terasology.workstation.process;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.workstation.process.inventory.InventoryInputComponent;
import org.terasology.workstation.processPart.ProcessEntityFinishExecutionEvent;
import org.terasology.workstation.processPart.ProcessEntityGetDurationEvent;
import org.terasology.workstation.processPart.ProcessEntityIsInvalidEvent;
import org.terasology.workstation.processPart.ProcessEntityIsInvalidToStartEvent;
import org.terasology.workstation.processPart.ProcessEntityStartExecutionEvent;
import org.terasology.workstation.processPart.inventory.ProcessEntityIsInvalidForFluidEvent;
import org.terasology.workstation.processPart.inventory.ProcessEntityIsInvalidForInventoryItemEvent;
import org.terasology.workstation.processPart.metadata.ProcessEntityGetInputDescriptionEvent;
import org.terasology.workstation.processPart.metadata.ProcessEntityGetOutputDescriptionEvent;

/**
 * This is a template for handling all processing events, meant to get you started on a new process.
 */
public class TemplateProcessPartCommonSystem extends BaseComponentSystem {

    ///// Processing

    @ReceiveEvent
    public void validateProcess(ProcessEntityIsInvalidEvent event, EntityRef processEntity,
                                InventoryInputComponent inventoryInputComponent) {
    }

    @ReceiveEvent
    public void validateToStartExecution(ProcessEntityIsInvalidToStartEvent event, EntityRef processEntity,
                                         InventoryInputComponent inventoryInputComponent) {
    }

    @ReceiveEvent
    public void startExecution(ProcessEntityStartExecutionEvent event, EntityRef processEntity,
                               InventoryInputComponent inventoryInputComponent) {
    }

    @ReceiveEvent
    public void getDuration(ProcessEntityGetDurationEvent event, EntityRef processEntity,
                            InventoryInputComponent inventoryInputComponent) {
    }

    @ReceiveEvent
    public void finishExecution(ProcessEntityFinishExecutionEvent event, EntityRef entityRef,
                                InventoryInputComponent inventoryInputComponent) {
    }

    ///// Inventory

    @ReceiveEvent
    public void isValidInventoryItem(ProcessEntityIsInvalidForInventoryItemEvent event, EntityRef processEntity,
                                     InventoryInputComponent inventoryInputComponent) {
    }

    ///// Fluid

    @ReceiveEvent
    public void isValidFluid(ProcessEntityIsInvalidForFluidEvent event, EntityRef processEntity,
                             InventoryInputComponent inventoryInputComponent) {
    }


    ///// Metadata

    @ReceiveEvent
    public void getInputDescriptions(ProcessEntityGetInputDescriptionEvent event, EntityRef processEntity,
                                     InventoryInputComponent inventoryInputComponent) {

    }

    @ReceiveEvent
    public void getOutputDescriptions(ProcessEntityGetOutputDescriptionEvent event, EntityRef processEntity,
                                      InventoryInputComponent inventoryInputComponent) {

    }
}
