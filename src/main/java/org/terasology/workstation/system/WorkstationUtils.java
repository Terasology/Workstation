package org.terasology.workstation.system;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.delay.AddDelayedActionEvent;
import org.terasology.logic.delay.CancelDelayedActionEvent;
import org.terasology.logic.delay.GetDelayedActionEvent;
import org.terasology.workstation.component.WorkstationProcessingComponent;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.ProcessPart;
import org.terasology.workstation.process.WorkstationProcess;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public final class WorkstationUtils {
    public static final String WORKSTATION_PROCESSING = "Workstation:Processing";

    private WorkstationUtils() {
    }

    public static void startProcessing(EntityRef instigator, EntityRef workstation, WorkstationProcess process,
                                       String processId, String resultId, long gameTime) {
        List<ProcessPart> processParts = process.getProcessParts();

        long duration = 0;
        Set<String> validResults = new HashSet<>();
        for (ProcessPart processPart : processParts) {
            try {
                Set<String> results = processPart.validate(instigator, workstation);
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
            duration += processPart.getDuration(instigator, workstation, resultId);
        }

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

        for (ProcessPart processPart : processParts) {
            processPart.executeStart(instigator, workstation, resultId);
        }

        if (duration > 0) {
            WorkstationUtils.scheduleWorkstationWakeUpIfNecessary(workstation, gameTime);
        } else {
            finishProcessing(instigator, workstation, process.getProcessType(), workstationProcessing, processParts, resultId);
        }
    }

    public static void finishProcessing(EntityRef instigator, EntityRef workstation, String processType, WorkstationProcessingComponent workstationProcessing, List<ProcessPart> processParts, String resultId) {
        workstationProcessing.processes.remove(processType);

        if (workstationProcessing.processes.size() > 0) {
            workstation.saveComponent(workstationProcessing);
        } else {
            workstation.removeComponent(WorkstationProcessingComponent.class);
        }

        for (ProcessPart processPart : processParts) {
            processPart.executeEnd(instigator, workstation, resultId);
        }
    }

    public static void scheduleWorkstationWakeUpIfNecessary(EntityRef workstation, long currentTime) {
        WorkstationProcessingComponent workstationProcessing = workstation.getComponent(WorkstationProcessingComponent.class);
        if (workstationProcessing != null) {
            long minEndTime = Long.MAX_VALUE;
            for (WorkstationProcessingComponent.ProcessDef processDef : workstationProcessing.processes.values()) {
                minEndTime = Math.min(minEndTime, processDef.processingFinishTime);
            }

            GetDelayedActionEvent getDelayed = new GetDelayedActionEvent();
            workstation.send(getDelayed);
            if (getDelayed.getActionId() != null) {
                workstation.send(new CancelDelayedActionEvent());
            }
            workstation.send(new AddDelayedActionEvent(WORKSTATION_PROCESSING, minEndTime - currentTime));
        }
    }
}
