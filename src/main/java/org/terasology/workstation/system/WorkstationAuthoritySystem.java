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
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
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
import org.terasology.workstation.event.WotkstationStateChanged;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.WorkstationProcess;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class WorkstationAuthoritySystem extends BaseComponentSystem {

    @In
    private WorkstationRegistry workstationRegistry;
    @In
    private Time time;

    private boolean executingProcess;

    private Set<EntityRef> pendingWorkstationChecks = new LinkedHashSet<>();

    @ReceiveEvent(components = {WorkstationComponent.class})
    public void userActivatesWorkstation(ActivateEvent event, EntityRef entity) {
        entity.send(new OpenWorkstationRequest());
    }

    @ReceiveEvent
    public void machineAdded(OnAddedComponent event, EntityRef entity, WorkstationComponent workstationComponent, WorkstationComponent workstation) {
        pendingWorkstationChecks.add(entity);
        startProcessingIfNotExecuting();
    }

    @ReceiveEvent
    public void automaticProcessingStateChanged(WotkstationStateChanged event, EntityRef entity, WorkstationComponent workstationComponent, WorkstationComponent workstation) {
        pendingWorkstationChecks.add(entity);
        startProcessingIfNotExecuting();
    }

    @ReceiveEvent
    public void finishProcessing(DelayedActionTriggeredEvent event, EntityRef workstation, WorkstationComponent workstationComp,
                                 WorkstationProcessingComponent workstationProcessing) {
        executingProcess = true;
        try {
            String actionId = event.getActionId();
            if (actionId.equals(WorkstationUtils.WORKSTATION_PROCESSING)) {
                long gameTime = time.getGameTimeInMs();
                Map<String, WorkstationProcessingComponent.ProcessDef> processesCopy = new HashMap<>(workstationProcessing.processes);
                for (Map.Entry<String, WorkstationProcessingComponent.ProcessDef> processes : processesCopy.entrySet()) {
                    WorkstationProcessingComponent.ProcessDef processDef = processes.getValue();
                    if (processDef.processingFinishTime <= gameTime) {
                        List<ProcessPart> processParts = workstationRegistry.
                                getWorkstationProcessById(workstationComp.supportedProcessTypes.keySet(), processDef.processingProcessId).
                                getProcessParts();
                        WorkstationUtils.finishProcessing(workstation, workstation, processes.getKey(), workstationProcessing,
                                processParts, processDef.processingResultId);
                    }
                }

                WorkstationUtils.scheduleWorkstationWakeUpIfNecessary(workstation, time.getGameTimeInMs());

                processPendingChecks();
            }
        } finally {
            executingProcess = false;
        }
    }

    @ReceiveEvent
    public void manualWorkstationProcess(WorkstationProcessRequest event, EntityRef workstation, WorkstationComponent workstationComp) {
        String processId = event.getProcessId();
        String resultId = event.getResultId();

        WorkstationProcess process = workstationRegistry.getWorkstationProcessById(workstationComp.supportedProcessTypes.keySet(), processId);
        if (process != null) {
            String processType = process.getProcessType();

            WorkstationProcessingComponent workstationProcessing = workstation.getComponent(WorkstationProcessingComponent.class);
            // It's not processing anything, or not processing this type of process
            if (workstationProcessing == null || !workstationProcessing.processes.containsKey(processType)) {
                executingProcess = true;
                try {
                    WorkstationUtils.startProcessing(event.getInstigator(), workstation, process, processId, resultId, time.getGameTimeInMs());
                } finally {
                    executingProcess = false;
                }
            }
        }
    }

    private void startProcessingIfNotExecuting() {
        // This is to avoid a new process starting in a middle of some other process executing
        if (!executingProcess) {
            executingProcess = true;
            try {
                processPendingChecks();
            } finally {
                executingProcess = false;
            }
        }
    }

    private void processPendingChecks() {
        while (!pendingWorkstationChecks.isEmpty()) {
            EntityRef workstation = extractFirstPendingWorkstation();
            if (workstation.exists()) {
                WorkstationComponent workstationComp = workstation.getComponent(WorkstationComponent.class);
                if (workstationComp != null) {
                    processIfHasPendingAutomaticProcesses(workstation, workstationComp);
                }
            }
        }
    }

    private EntityRef extractFirstPendingWorkstation() {
        Iterator<EntityRef> checksIterator = pendingWorkstationChecks.iterator();
        EntityRef workstation = checksIterator.next();
        checksIterator.remove();
        return workstation;
    }

    private void processIfHasPendingAutomaticProcesses(EntityRef entity, WorkstationComponent workstation) {
        Map<String, Boolean> possibleProcesses = new LinkedHashMap<>(workstation.supportedProcessTypes);

        // Filter out those currently processing
        WorkstationProcessingComponent processing = entity.getComponent(WorkstationProcessingComponent.class);
        if (processing != null) {
            for (String processed : processing.processes.keySet()) {
                possibleProcesses.remove(processed);
            }
        }

        // Filter out non-automatic
        for (Map.Entry<String, Boolean> processDef : workstation.supportedProcessTypes.entrySet()) {
            if (processDef.getValue() == false) {
                possibleProcesses.remove(processDef.getKey());
            }
        }

        for (WorkstationProcess workstationProcess : workstationRegistry.getWorkstationProcesses(possibleProcesses.keySet())) {
            if (possibleProcesses.get(workstationProcess.getId())) {
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
