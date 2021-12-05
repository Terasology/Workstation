// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.ui;

import com.google.common.base.Preconditions;
import org.joml.Vector2i;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.nui.Canvas;
import org.terasology.nui.CoreWidget;
import org.terasology.nui.layouts.ColumnLayout;
import org.terasology.workstation.component.WorkstationComponent;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.system.WorkstationRegistry;

import java.util.Collection;
import java.util.Comparator;

/**
 * Lists processes related to the passed in workstation
 */
public class ProcessListWidget extends CoreWidget implements WorkstationUI {
    EntityRef workstation;
    ColumnLayout columnLayout;

    public ProcessListWidget() {
    }

    @Override
    public void onDraw(Canvas canvas) {
        columnLayout.onDraw(canvas);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return columnLayout.getPreferredContentSize(canvas, sizeHint);
    }

    @Override
    public void initializeWorkstation(EntityRef workstationEntity) {
        Preconditions.checkArgument(workstation.hasComponent(WorkstationComponent.class));

        this.workstation = workstationEntity;

        // loop through all relevant processes and add a representation to the screen
        WorkstationRegistry workstationRegistry = CoreRegistry.get(WorkstationRegistry.class);
        WorkstationComponent workstationComponent = workstation.getComponent(WorkstationComponent.class);

        columnLayout = new ColumnLayout();

        Collection<WorkstationProcess> processes =
                workstationRegistry.getWorkstationProcesses(workstationComponent.supportedProcessTypes.keySet());

        processes.stream()
                .sorted(Comparator.comparing(WorkstationProcess::getProcessTypeName))
                .forEach(process -> columnLayout.addWidget(new ProcessSummaryWidget(process)));
    }
}
