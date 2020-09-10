// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.Replicate;
import org.terasology.nui.reflection.MappedContainer;

import java.util.HashMap;
import java.util.Map;

public class WorkstationProcessingComponent implements Component {
    @Replicate
    public Map<String, ProcessDef> processes = new HashMap<>();

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
