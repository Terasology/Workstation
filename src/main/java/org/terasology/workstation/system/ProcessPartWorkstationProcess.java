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

import com.google.common.collect.Lists;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.workstation.component.ProcessDefinitionComponent;
import org.terasology.workstation.event.WorkstationProcessRequest;
import org.terasology.workstation.process.DescribeProcess;
import org.terasology.workstation.process.InvalidProcessException;
import org.terasology.workstation.process.InvalidProcessPartException;
import org.terasology.workstation.process.ProcessPartDescription;
import org.terasology.workstation.process.ValidateProcess;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.process.fluid.ValidateFluidInventoryItem;
import org.terasology.workstation.processPart.ProcessEntityFinishExecutionEvent;
import org.terasology.workstation.processPart.ProcessEntityGetDurationEvent;
import org.terasology.workstation.processPart.ProcessEntityIsInvalidEvent;
import org.terasology.workstation.processPart.ProcessEntityIsInvalidToStartEvent;
import org.terasology.workstation.processPart.ProcessEntityStartExecutionEvent;
import org.terasology.workstation.processPart.inventory.ProcessEntityIsInvalidForFluidEvent;
import org.terasology.workstation.processPart.inventory.ProcessEntityIsInvalidForInventoryItemEvent;
import org.terasology.workstation.processPart.metadata.ProcessEntityGetInputDescriptionEvent;
import org.terasology.workstation.processPart.metadata.ProcessEntityGetOutputDescriptionEvent;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Order of events for the most simple processing path: - ProcessEntityIsInvalidEvent (this happens once when loaded) -
 */
public class ProcessPartWorkstationProcess implements WorkstationProcess, ValidateInventoryItem,
        ValidateFluidInventoryItem, DescribeProcess, ValidateProcess {
    private String id;
    private ProcessDefinitionComponent processDefinitionComponent;
    private String processTypeName;
    private Prefab prefab;

    private EntityManager entityManager;

    ProcessPartWorkstationProcess(Prefab prefab, EntityManager entityManager) throws InvalidProcessPartException {
        this.entityManager = entityManager;
        this.prefab = prefab;
        id = "Prefab:" + prefab.getUrn().toString();
        processDefinitionComponent = prefab.getComponent(ProcessDefinitionComponent.class);
        processTypeName = prefab.getUrn().toString();

        EntityRef tempProcessEntity = createProcessEntity(false);
        ProcessEntityIsInvalidEvent processEntityIsInvalidEvent = new ProcessEntityIsInvalidEvent();
        tempProcessEntity.send(processEntityIsInvalidEvent);
        tempProcessEntity.destroy();

        if (processEntityIsInvalidEvent.hasErrors()) {
            throw new InvalidProcessPartException(String.join(System.lineSeparator(),
                    processEntityIsInvalidEvent.getErrors()));
        }
    }

    @Override
    public EntityRef createProcessEntity() {
        return createProcessEntity(true);
    }

    private EntityRef createProcessEntity(boolean persistant) {
        EntityBuilder builder = entityManager.newBuilder(prefab);
        builder.setPersistent(persistant);
        return builder.build();
    }

    @Override
    public boolean isValid(EntityRef workstation, int slotNo, EntityRef instigator, EntityRef item) {
        ProcessEntityIsInvalidForInventoryItemEvent event =
                new ProcessEntityIsInvalidForInventoryItemEvent(workstation, slotNo, instigator, item);
        EntityRef tempProcessEntity = createProcessEntity(false);
        tempProcessEntity.send(event);
        tempProcessEntity.destroy();
        return !event.isConsumed();
    }

    @Override
    public boolean isValid(EntityRef instigator, EntityRef workstation) {
        ProcessEntityIsInvalidToStartEvent event = new ProcessEntityIsInvalidToStartEvent(instigator, workstation);
        EntityRef tempProcessEntity = createProcessEntity(false);
        tempProcessEntity.send(event);
        tempProcessEntity.destroy();
        return !event.isConsumed();
    }

    @Override
    public boolean isValidFluid(EntityRef workstation, int slotNo, EntityRef instigator, String fluidType) {
        ProcessEntityIsInvalidForFluidEvent event = new ProcessEntityIsInvalidForFluidEvent(workstation, slotNo,
                instigator, fluidType);
        EntityRef tempProcessEntity = createProcessEntity(false);
        tempProcessEntity.send(event);
        tempProcessEntity.destroy();
        return !event.isConsumed();
    }

    @Override
    public String getProcessType() {
        return processDefinitionComponent.processType;
    }

    @Override
    public String getProcessTypeName() {
        return processTypeName;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long startProcessingManual(EntityRef instigator, EntityRef workstation, WorkstationProcessRequest
            request, EntityRef processEntity) throws InvalidProcessException {
        return startProcessing(instigator, workstation, processEntity);
    }

    @Override
    public long startProcessingAutomatic(EntityRef workstation, EntityRef processEntity) throws
            InvalidProcessException {
        return startProcessing(workstation, workstation, processEntity);
    }

    private long startProcessing(EntityRef instigator, EntityRef workstation, EntityRef processEntity) throws InvalidProcessException {
        // Is the process entity valid to execute?
        ProcessEntityIsInvalidToStartEvent event = new ProcessEntityIsInvalidToStartEvent(instigator, workstation);
        processEntity.send(event);
        if (event.isConsumed()) {
            throw new InvalidProcessException();
        }

        // Execute the process!
        ProcessEntityStartExecutionEvent processEntityStartExecutionEvent =
                new ProcessEntityStartExecutionEvent(instigator, workstation);
        processEntity.send(processEntityStartExecutionEvent);

        // How long does this process take before calling finish?
        ProcessEntityGetDurationEvent durationEvent = new ProcessEntityGetDurationEvent(0f, workstation, instigator);
        processEntity.send(durationEvent);

        return (long) (durationEvent.getResultValue() * 1000f);
    }

    @Override
    public void finishProcessing(EntityRef instigator, EntityRef workstation, EntityRef processEntity) {
        // Finish processing, hopefully generating the desired results
        ProcessEntityFinishExecutionEvent processEntityFinishExecutionEvent =
                new ProcessEntityFinishExecutionEvent(instigator, workstation);
        processEntity.send(processEntityFinishExecutionEvent);
    }

    @Override
    public Collection<ProcessPartDescription> getOutputDescriptions() {
        ProcessEntityGetOutputDescriptionEvent event = new ProcessEntityGetOutputDescriptionEvent();
        EntityRef tempProcessEntity = createProcessEntity(false);
        tempProcessEntity.send(event);
        tempProcessEntity.destroy();

        List<ProcessPartDescription> result = Lists.newLinkedList(event.getOutputDescriptions());
        result.sort(Comparator.comparing(ProcessPartDescription::getDisplayName));
        return result;
    }

    @Override
    public Collection<ProcessPartDescription> getInputDescriptions() {
        ProcessEntityGetInputDescriptionEvent event = new ProcessEntityGetInputDescriptionEvent();
        EntityRef tempProcessEntity = createProcessEntity(false);
        tempProcessEntity.send(event);
        tempProcessEntity.destroy();

        List<ProcessPartDescription> result = Lists.newLinkedList(event.getInputDescriptions());
        result.sort(Comparator.comparing(ProcessPartDescription::getDisplayName));
        return result;
    }
}
