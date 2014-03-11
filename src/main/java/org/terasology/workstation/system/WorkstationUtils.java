package org.terasology.workstation.system;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.delay.AddDelayedActionEvent;
import org.terasology.logic.delay.CancelDelayedActionEvent;
import org.terasology.logic.delay.HasDelayedActionEvent;
import org.terasology.registry.CoreRegistry;
import org.terasology.workstation.component.WorkstationProcessingComponent;
import org.terasology.workstation.event.WorkstationProcessRequest;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.WorkstationProcess;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public final class WorkstationUtils {
    public static final String WORKSTATION_PROCESSING = "Workstation:Processing";

    private WorkstationUtils() {
    }

    public static void startProcessingManual(EntityRef instigator, EntityRef workstation, WorkstationProcess process,
                                             WorkstationProcessRequest request, long gameTime) {
        final EntityManager entityManager = CoreRegistry.get(EntityManager.class);

        final EntityRef processEntity = entityManager.create();

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
                WorkstationUtils.scheduleWorkstationWakeUpIfNecessary(workstation, gameTime);
            } else {
                finishProcessing(instigator, workstation, process, workstationProcessing);
            }
        } catch (InvalidProcessException exp) {
            processEntity.destroy();
        }
    }

    public static void startProcessingAutomatic(EntityRef workstation, WorkstationProcess process, long gameTime) {
        final EntityManager entityManager = CoreRegistry.get(EntityManager.class);

        final EntityRef processEntity = entityManager.create();

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

    public static void finishProcessing(EntityRef instigator, EntityRef workstation, WorkstationProcess process) {
        finishProcessing(instigator, workstation, process, workstation.getComponent(WorkstationProcessingComponent.class));
    }

    public static void scheduleWorkstationWakeUpIfNecessary(EntityRef workstation, long currentTime) {
        WorkstationProcessingComponent workstationProcessing = workstation.getComponent(WorkstationProcessingComponent.class);
        if (workstationProcessing != null) {
            long minEndTime = Long.MAX_VALUE;
            for (WorkstationProcessingComponent.ProcessDef processDef : workstationProcessing.processes.values()) {
                minEndTime = Math.min(minEndTime, processDef.processingFinishTime);
            }

            HasDelayedActionEvent getDelayed = new HasDelayedActionEvent(WORKSTATION_PROCESSING);
            workstation.send(getDelayed);
            if (getDelayed.hasAction()) {
                workstation.send(new CancelDelayedActionEvent(WORKSTATION_PROCESSING));
            }
            workstation.send(new AddDelayedActionEvent(WORKSTATION_PROCESSING, minEndTime - currentTime));
        }
    }
}
