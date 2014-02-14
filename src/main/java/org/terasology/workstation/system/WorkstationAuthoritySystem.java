/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.registry.In;
import org.terasology.workstation.component.WorkstationComponent;
import org.terasology.workstation.component.WorkstationProcessingComponent;
import org.terasology.workstation.event.OpenWorkstationRequest;
import org.terasology.workstation.event.WorkstationProcessRequest;
import org.terasology.workstation.process.WorkstationProcess;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class WorkstationAuthoritySystem extends BaseComponentSystem {

    @In
    private WorkstationRegistry workstationRegistry;
    @In
    private Time time;

    @ReceiveEvent(components = {WorkstationComponent.class})
    public void userActivatesWorkstation(ActivateEvent event, EntityRef entity) {
        entity.send(new OpenWorkstationRequest());
    }

    @ReceiveEvent
    public void finishProcessing(DelayedActionTriggeredEvent event, EntityRef workstation, WorkstationComponent workstationComp, WorkstationProcessingComponent workstationProcessing) {
        String actionId = event.getActionId();
        if (actionId.equals(WorkstationUtils.WORKSTATION_PROCESSING)) {
            long gameTime = time.getGameTimeInMs();
            Map<String, WorkstationProcessingComponent.ProcessDef> processesCopy = new HashMap<>(workstationProcessing.processes);
            for (Map.Entry<String, WorkstationProcessingComponent.ProcessDef> processes : processesCopy.entrySet()) {
                WorkstationProcessingComponent.ProcessDef processDef = processes.getValue();
                if (processDef.processingFinishTime <= gameTime) {
                    WorkstationUtils.finishProcessing(workstation, processes.getKey(), workstationProcessing,
                            workstationRegistry.getWorkstationProcessById(workstationComp.supportedProcessTypes, processDef.processingProcessId).getProcessParts(), processDef.processingResultId);
                }
            }

            WorkstationUtils.scheduleWorkstationWakeUpIfNecessary(workstation, time.getGameTimeInMs());
        }
    }

    @ReceiveEvent
    public void manualWorkstationProcess(WorkstationProcessRequest event, EntityRef workstation, WorkstationComponent workstationComp) {
        String processId = event.getProcessId();
        String resultId = event.getResultId();

        WorkstationProcess process = workstationRegistry.getWorkstationProcessById(workstationComp.supportedProcessTypes, processId);
        if (process != null) {
            String processType = process.getProcessType();

            WorkstationProcessingComponent workstationProcessing = workstation.getComponent(WorkstationProcessingComponent.class);
            // It's not processing anything, or not processing this type of process
            if (workstationProcessing == null || !workstationProcessing.processes.containsKey(processType)) {
                WorkstationUtils.startProcessing(event.getInstigator(), workstation, process, processId, resultId, time.getGameTimeInMs());
            }
        }
    }
}
