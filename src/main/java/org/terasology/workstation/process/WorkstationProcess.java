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
package org.terasology.workstation.process;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.workstation.event.WorkstationProcessRequest;

public interface WorkstationProcess {
    String getId();

    String getProcessType();

    int getProcessLevel();

    default String getProcessTypeName() {
        return getProcessType();
    }

    long startProcessingAutomatic(EntityRef workstation, EntityRef processEntity) throws InvalidProcessException;

    long startProcessingManual(EntityRef instigator, EntityRef workstation, WorkstationProcessRequest request, EntityRef processEntity) throws InvalidProcessException;

    void finishProcessing(EntityRef instigator, EntityRef workstation, EntityRef processEntity);

    // allow a WorkstationProcess to override the default blank entity creation with something fancier
    default EntityRef createProcessEntity() {
        return null;
    }
}
