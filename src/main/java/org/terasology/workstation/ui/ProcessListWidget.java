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
package org.terasology.workstation.ui;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.geom.Vector2i;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.workstation.component.WorkstationComponent;
import org.terasology.workstation.component.WorkstationProcessType;
import org.terasology.workstation.process.WorkstationProcess;
import org.terasology.workstation.system.WorkstationRegistry;

import java.util.ArrayList;

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
        for (WorkstationProcess process : workstationRegistry.getWorkstationProcesses(new ArrayList<WorkstationProcessType>(workstationComponent.supportedProcessTypes.values()))) {
            // add a description of each process to the layout
            columnLayout.addWidget(new ProcessSummaryWidget(process));
        }
    }
}
