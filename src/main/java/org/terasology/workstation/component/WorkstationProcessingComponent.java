// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.component;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.reflection.MappedContainer;

import java.util.HashMap;
import java.util.Map;

public class WorkstationProcessingComponent implements Component<WorkstationProcessingComponent> {
    @Replicate
    public Map<String, ProcessDef> processes = new HashMap<>();

    @Override
    public void copy(WorkstationProcessingComponent other) {
        this.processes.clear();
        for (Map.Entry<String, ProcessDef> entry : other.processes.entrySet()) {
            ProcessDef oldDef = entry.getValue();
            ProcessDef newDef = new ProcessDef();
            newDef.processingProcessId = oldDef.processingProcessId;
            newDef.processEntity = oldDef.processEntity;
            newDef.processingStartTime = oldDef.processingStartTime;
            newDef.processingFinishTime = oldDef.processingFinishTime;
            this.processes.put(entry.getKey(), newDef);
        }
    }

    @MappedContainer
    public static class ProcessDef {
        @Replicate
        public String processingProcessId;
        @Replicate
        public EntityRef processEntity;
        @Replicate
        public long processingStartTime;
        @Replicate
        public long processingFinishTime;
    }
}
