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
import org.terasology.logic.delay.AddDelayedActionEvent;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.registry.In;
import org.terasology.workstation.component.WorkstationComponent;
import org.terasology.workstation.component.WorkstationProcessingComponent;
import org.terasology.workstation.event.OpenWorkstationRequest;
import org.terasology.workstation.event.WorkstationProcessRequest;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.WorkstationProcess;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class WorkstationAuthoritySystem extends BaseComponentSystem {
    private static final String WORKSTATION_PROCESSING = "Workstation:Processing";

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
        if (actionId.equals(WORKSTATION_PROCESSING)) {
            long gameTime = time.getGameTimeInMs();
            for (Map.Entry<String, WorkstationProcessingComponent.ProcessDef> processes : workstationProcessing.processes.entrySet()) {
                if (processes.getValue().processingFinishTime <= gameTime) {
                    finishProcessing(workstation, workstationComp, processes.getKey(), workstationProcessing);
                }
            }

            scheduleWorkstationWakeUpIfNecessary(workstation);
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
                startProcessing(event, workstation, workstationComp, processId, resultId, process);
                scheduleWorkstationWakeUpIfNecessary(workstation);
            }
        }
    }

    private void scheduleWorkstationWakeUpIfNecessary(EntityRef workstation) {
        WorkstationProcessingComponent workstationProcessing = workstation.getComponent(WorkstationProcessingComponent.class);
        if (workstationProcessing != null) {
            long minEndTime = Long.MAX_VALUE;
            for (WorkstationProcessingComponent.ProcessDef processDef : workstationProcessing.processes.values()) {
                minEndTime = Math.min(minEndTime, processDef.processingFinishTime);
            }

            workstation.send(new AddDelayedActionEvent(WORKSTATION_PROCESSING, minEndTime - time.getGameTimeInMs()));
        }
    }

    private void startProcessing(WorkstationProcessRequest event, EntityRef workstation, WorkstationComponent workstationComp, String processId, String resultId, WorkstationProcess process) {
        List<ProcessPart> processParts = process.getProcessParts();

        long duration = 0;
        Set<String> validResults = new HashSet<>();
        for (ProcessPart processPart : processParts) {
            try {
                Set<String> results = processPart.validate(event.getInstigator(), workstation);
                if (results != null) {
                    validResults.addAll(results);
                }
            } catch (InvalidProcessException exp) {
                return;
            }
        }

        if (resultId != null && !validResults.contains(resultId)) {
            return;
        }

        for (ProcessPart processPart : processParts) {
            duration += processPart.getDuration(event.getInstigator(), workstation, resultId);
        }

        for (ProcessPart processPart : processParts) {
            processPart.executeStart(event.getInstigator(), workstation, resultId);
        }

        if (duration > 0) {
            long gameTime = time.getGameTimeInMs();

            WorkstationProcessingComponent.ProcessDef processDef = new WorkstationProcessingComponent.ProcessDef();
            processDef.processingStartTime = gameTime;
            processDef.processingFinishTime = gameTime + duration;
            processDef.processingProcessId = processId;
            processDef.processingResultId = resultId;

            WorkstationProcessingComponent workstationProcessing = workstation.getComponent(WorkstationProcessingComponent.class);
            if (workstationProcessing == null) {
                workstationProcessing = new WorkstationProcessingComponent();
                workstationProcessing.processes.put(process.getProcessType(), processDef);
                workstation.addComponent(workstationProcessing);
            } else {
                workstationProcessing.processes.put(process.getProcessType(), processDef);
                workstation.saveComponent(workstationProcessing);
            }
        } else {
            for (ProcessPart processPart : processParts) {
                processPart.executeEnd(event.getInstigator(), workstation, resultId);
            }
        }
    }

    private void finishProcessing(EntityRef workstation, WorkstationComponent workstationComp, String processType, WorkstationProcessingComponent workstationProcessing) {
        WorkstationProcessingComponent.ProcessDef processDef = workstationProcessing.processes.get(processType);
        WorkstationProcess process = workstationRegistry.getWorkstationProcessById(workstationComp.supportedProcessTypes, processDef.processingProcessId);

        for (ProcessPart processPart : process.getProcessParts()) {
            processPart.executeEnd(workstation, workstation, processDef.processingResultId);
        }

        workstationProcessing.processes.remove(processType);

        if (workstationProcessing.processes.size() > 0) {
            workstation.saveComponent(workstationProcessing);
        } else {
            workstation.removeComponent(WorkstationProcessingComponent.class);
        }
    }

}
