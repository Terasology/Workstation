// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.ui;

import org.terasology.entitySystem.entity.EntityRef;
import org.joml.Vector2i;
import org.terasology.registry.CoreRegistry;
import org.terasology.nui.Canvas;
import org.terasology.nui.CoreWidget;
import org.terasology.nui.layouts.ColumnLayout;
import org.terasology.workstation.component.WorkstationComponent;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.system.WorkstationRegistry;

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
        this.workstation = workstationEntity;

        // loop through all relevant processes and add a representation to the screen
        WorkstationRegistry workstationRegistry = CoreRegistry.get(WorkstationRegistry.class);
        WorkstationComponent workstationComponent = workstation.getComponent(WorkstationComponent.class);

        columnLayout = new ColumnLayout();
        for (WorkstationProcess process : workstationRegistry.getWorkstationProcesses(workstationComponent.supportedProcessTypes.keySet())) {
            // add a description of each process to the layout
            columnLayout.addWidget(new ProcessSummaryWidget(process));
        }
    }
}
