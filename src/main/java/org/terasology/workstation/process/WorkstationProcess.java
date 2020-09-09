// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.workstation.event.WorkstationProcessRequest;

public interface WorkstationProcess {
    String getId();

    String getProcessType();

    default String getProcessTypeName() {
        return getProcessType();
    }

    long startProcessingAutomatic(EntityRef workstation, EntityRef processEntity) throws InvalidProcessException;

    long startProcessingManual(EntityRef instigator, EntityRef workstation, WorkstationProcessRequest request,
                               EntityRef processEntity) throws InvalidProcessException;

    void finishProcessing(EntityRef instigator, EntityRef workstation, EntityRef processEntity);

    // allow a WorkstationProcess to override the default blank entity creation with something fancier
    default EntityRef createProcessEntity() {
        return null;
    }
}
