// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.system;

import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.workstation.component.WorkstationComponent;
import org.terasology.workstation.component.WorkstationProcessingComponent;
import org.terasology.workstation.event.WorkstationProcessRequest;
import org.terasology.workstation.event.WorkstationStateChanged;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.WorkstationProcess;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

@RegisterSystem(RegisterMode.AUTHORITY)
public class WorkstationAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    public static final String WORKSTATION_PROCESSING = "Workstation:Processing";

    private static final int AUTOMATIC_PROCESSING_REVIVAL_INTERVAL = 10000;

    @In
    private WorkstationRegistry workstationRegistry;
    @In
    private Time time;
    @In
    private EntityManager entityManager;

    private boolean executingProcess;
    private long nextAutomaticProcessingRevivalTime;

    // I would rather use LinkedHashSet, however cannot due to PojoEntityRef's hashcode changing when it is being destroyed.
    private Deque<EntityRef> pendingWorkstationChecks = new LinkedList<>();

    @ReceiveEvent
    public void machineAdded(OnAddedComponent event, EntityRef workstation, WorkstationComponent workstationComponent, BlockComponent block) {
        pendingWorkstationChecks.add(workstation);
        startProcessingIfNotExecuting();
    }

    @ReceiveEvent
    public void automaticProcessingStateChanged(WorkstationStateChanged event, EntityRef workstation, WorkstationComponent workstationComponent) {
        pendingWorkstationChecks.add(workstation);
        startProcessingIfNotExecuting();
    }

    @ReceiveEvent
    public void finishProcessing(DelayedActionTriggeredEvent event, EntityRef workstation, WorkstationComponent workstationComp,
                                 WorkstationProcessingComponent workstationProcessing) {
        PerformanceMonitor.startActivity("Workstation - finishing process");
        executingProcess = true;
        try {
            String actionId = event.getActionId();
            long gameTime = time.getGameTimeInMs();
            Map<String, WorkstationProcessingComponent.ProcessDef> processesCopy = new HashMap<>(workstationProcessing.processes);
            for (Map.Entry<String, WorkstationProcessingComponent.ProcessDef> processes : processesCopy.entrySet()) {
                WorkstationProcessingComponent.ProcessDef processDef = processes.getValue();
                if (processDef.processingFinishTime <= gameTime) {
                    final WorkstationProcess workstationProcess = workstationRegistry.getWorkstationProcessById(
                            workstationComp.supportedProcessTypes.keySet(), processDef.processingProcessId);
                    finishProcessing(workstation, workstation, workstationProcess);
                }
            }

            pendingWorkstationChecks.add(workstation);
            processPendingChecks();
        } finally {
            executingProcess = false;
            PerformanceMonitor.endActivity();
        }
    }

    @ReceiveEvent
    public void manualWorkstationProcess(WorkstationProcessRequest event, EntityRef instigator) {
        EntityRef workstation = event.getWorkstation();
        String processId = event.getProcessId();
        WorkstationComponent workstationComp = workstation.getComponent(WorkstationComponent.class);

        if (workstationComp != null) {
            WorkstationProcess process = workstationRegistry.getWorkstationProcessById(workstationComp.supportedProcessTypes.keySet(), processId);
            if (process != null) {
                String processType = process.getProcessType();

                WorkstationProcessingComponent workstationProcessing = workstation.getComponent(WorkstationProcessingComponent.class);
                // It's not processing anything, or not processing this type of process
                if (workstationProcessing == null || !workstationProcessing.processes.containsKey(processType)) {
                    executingProcess = true;
                    try {
                        startProcessingManual(instigator, workstation, process, event, time.getGameTimeInMs());
                    } finally {
                        executingProcess = false;
                    }
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
        PerformanceMonitor.startActivity("Workstation - processing pending checks");
        try {
            while (!pendingWorkstationChecks.isEmpty()) {
                EntityRef workstation = extractFirstPendingWorkstation();
                if (workstation.exists()) {
                    WorkstationComponent workstationComp = workstation.getComponent(WorkstationComponent.class);
                    if (workstationComp != null) {
                        processIfHasPendingAutomaticProcesses(workstation, workstationComp);
                    }
                }
            }
        } finally {
            PerformanceMonitor.endActivity();
        }
    }

    private EntityRef extractFirstPendingWorkstation() {
        return pendingWorkstationChecks.removeFirst();
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
            if (!processDef.getValue()) {
                possibleProcesses.remove(processDef.getKey());
            }
        }

        for (WorkstationProcess workstationProcess : workstationRegistry.getWorkstationProcesses(possibleProcesses.keySet())) {
            if (possibleProcesses.get(workstationProcess.getProcessType())) {
                startProcessingAutomatic(entity, workstationProcess, time.getGameTimeInMs());
            }
        }
    }

    @Override
    public void update(float delta) {
        long currentTime = time.getGameTimeInMs();
        if (currentTime > nextAutomaticProcessingRevivalTime) {
            nextAutomaticProcessingRevivalTime = currentTime + AUTOMATIC_PROCESSING_REVIVAL_INTERVAL;
            for (EntityRef entityRef : entityManager.getEntitiesWith(WorkstationComponent.class, BlockComponent.class)) {
                WorkstationComponent workstationComponent = entityRef.getComponent(WorkstationComponent.class);
                if (workstationComponent.supportedProcessTypes.containsValue(true)
                        && !entityRef.hasComponent(WorkstationProcessingComponent.class)) {
                    // there are automatic processes and there is no process currently running, trigger a state change event
                    // sometimes automatic workstations need to be jump started after world load or if there are not sufficient state changed events
                    entityRef.send(new WorkstationStateChanged());
                }
            }
        }
    }

    private void startProcessingManual(EntityRef instigator, EntityRef workstation, WorkstationProcess process,
                                       WorkstationProcessRequest request, long gameTime) {
        EntityRef processEntity = process.createProcessEntity();
        if (processEntity == null || processEntity == EntityRef.NULL) {
            // create a blank process entity;
            processEntity = entityManager.create();
        }

        try {
            final long duration = process.startProcessingManual(instigator, workstation, request, processEntity);

            WorkstationProcessingComponent.ProcessDef processDef = new WorkstationProcessingComponent.ProcessDef();
            processDef.processingStartTime = gameTime;
            processDef.processingFinishTime = gameTime + duration;
            processDef.processingProcessId = process.getId();
            processDef.processEntity = processEntity;

            WorkstationProcessingComponent workstationProcessing = workstation.getComponent(WorkstationProcessingComponent.class);
            if (workstationProcessing == null) {
                workstationProcessing = new WorkstationProcessingComponent();
                workstationProcessing.processes.put(process.getProcessType(), processDef);
                workstation.addComponent(workstationProcessing);
            } else {
                workstationProcessing.processes.put(process.getProcessType(), processDef);
                workstation.saveComponent(workstationProcessing);
            }

            if (duration > 0) {
                scheduleWorkstationWakeUpIfNecessary(workstation, gameTime);
            } else {
                finishProcessing(workstation, workstation, process, workstationProcessing);
            }
        } catch (InvalidProcessException exp) {
            processEntity.destroy();
        }
    }

    private void startProcessingAutomatic(EntityRef workstation, WorkstationProcess process, long gameTime) {
        EntityRef processEntity = process.createProcessEntity();
        if (processEntity == null || processEntity == EntityRef.NULL) {
            // create a blank process entity;
            processEntity = entityManager.create();
        }

        try {
            final long duration = process.startProcessingAutomatic(workstation, processEntity);

            WorkstationProcessingComponent.ProcessDef processDef = new WorkstationProcessingComponent.ProcessDef();
            processDef.processingStartTime = gameTime;
            processDef.processingFinishTime = gameTime + duration;
            processDef.processingProcessId = process.getId();
            processDef.processEntity = processEntity;

            WorkstationProcessingComponent workstationProcessing = workstation.getComponent(WorkstationProcessingComponent.class);
            if (workstationProcessing == null) {
                workstationProcessing = new WorkstationProcessingComponent();
                workstationProcessing.processes.put(process.getProcessType(), processDef);
                workstation.addComponent(workstationProcessing);
            } else {
                workstationProcessing.processes.put(process.getProcessType(), processDef);
                workstation.saveComponent(workstationProcessing);
            }

            if (duration > 0) {
                scheduleWorkstationWakeUpIfNecessary(workstation, gameTime);
            } else {
                finishProcessing(workstation, workstation, process, workstationProcessing);
            }
        } catch (InvalidProcessException exp) {
            processEntity.destroy();
        }
    }

    private static void finishProcessing(EntityRef instigator, EntityRef workstation, WorkstationProcess process, WorkstationProcessingComponent workstationProcessing) {
        final WorkstationProcessingComponent.ProcessDef processDef = workstationProcessing.processes.remove(process.getProcessType());

        if (workstationProcessing.processes.size() > 0) {
            workstation.saveComponent(workstationProcessing);
        } else {
            workstation.removeComponent(WorkstationProcessingComponent.class);
        }

        process.finishProcessing(instigator, workstation, processDef.processEntity);

        processDef.processEntity.destroy();
    }

    private void finishProcessing(EntityRef instigator, EntityRef workstation, WorkstationProcess process) {
        finishProcessing(instigator, workstation, process, workstation.getComponent(WorkstationProcessingComponent.class));
    }

    // If necessary, schedule wake-ups (i.e. DelayActions) for the processes in this workstation.
    private void scheduleWorkstationWakeUpIfNecessary(EntityRef workstation, long currentTime) {
        WorkstationProcessingComponent workstationProcessing = workstation.getComponent(WorkstationProcessingComponent.class);
        if (workstationProcessing != null) {
            long endTime = Long.MAX_VALUE;

            final DelayManager delayManager = CoreRegistry.get(DelayManager.class);

            // Schedule wake-ups for all processes in this Workstation. TODO: Perhaps it could be done more efficiently?
            for (WorkstationProcessingComponent.ProcessDef processDef : workstationProcessing.processes.values()) {
                endTime = processDef.processingFinishTime;

                // Construct the unique action ID to prevent conflicts in the event handler.
                String fullActionID = WORKSTATION_PROCESSING + " - " +
                        processDef.processingProcessId + " - " + processDef.hashCode();

                // If there was a delayed action pertaining to this workstatio and action ID, cancel it.
                if (delayManager.hasDelayedAction(workstation, fullActionID)) {
                    delayManager.cancelDelayedAction(workstation, fullActionID);
                }

                // Add a delayed action pertaining to this workstation and action ID, and activate it after
                delayManager.addDelayedAction(workstation, fullActionID, endTime - currentTime);
            }
        }
    }
}
