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

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;
import org.terasology.workstation.component.AutomaticProcessingComponent;
import org.terasology.workstation.component.WorkstationComponent;
import org.terasology.workstation.component.WorkstationProcessingComponent;
import org.terasology.workstation.event.AutomaticProcessingStateChanged;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.WorkstationProcess;

import java.util.HashSet;
import java.util.Set;

@RegisterSystem(RegisterMode.AUTHORITY)
public class AutomaticProcessingSystem extends BaseComponentSystem {
    @In
    private WorkstationRegistry workstationRegistry;
    @In
    private Time time;

    @ReceiveEvent
    public void machineAdded(OnAddedComponent event, EntityRef entity, AutomaticProcessingComponent automaticProcessing, WorkstationComponent workstation) {
        checkForProcessing(entity, workstation);
    }

    @ReceiveEvent
    public void automaticProcessingStateChanged(AutomaticProcessingStateChanged event, EntityRef entity, AutomaticProcessingComponent automaticProcessing, WorkstationComponent workstation) {
        checkForProcessing(entity, workstation);
    }

    private void checkForProcessing(EntityRef entity, WorkstationComponent workstation) {
        Set<String> possibleProcesses = new HashSet<>(workstation.supportedProcessTypes);

        WorkstationProcessingComponent processing = entity.getComponent(WorkstationProcessingComponent.class);
        if (processing != null) {
            possibleProcesses.removeAll(processing.processes.keySet());
        }

        for (WorkstationProcess workstationProcess : workstationRegistry.getWorkstationProcesses(possibleProcesses)) {
            if (workstationProcess.isAutomatable()) {
                try {
                    Set<String> possibleResultIds = new HashSet<>();
                    for (ProcessPart processPart : workstationProcess.getProcessParts()) {
                        Set<String> resultIds = processPart.validate(entity, entity);
                        if (resultIds != null) {
                            possibleResultIds.addAll(resultIds);
                        }
                    }

                    if (possibleResultIds.size() <= 1) {
                        String resultId = possibleResultIds.size() == 0 ? null : possibleResultIds.iterator().next();
                        WorkstationUtils.startProcessing(entity, entity, workstationProcess, workstationProcess.getId(), resultId, time.getGameTimeInMs());
                    }
                } catch (InvalidProcessException exp) {
                    // Ignored - proceed to next process
                }
            }
        }
    }
}