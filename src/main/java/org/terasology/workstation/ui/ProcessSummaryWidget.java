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

import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.workstation.process.DescribeProcess;
import org.terasology.workstation.process.WorkstationProcess;

/**
 * Displays the input and output of a process
 */
public class ProcessSummaryWidget extends CoreWidget {
    UILabel description;

    public ProcessSummaryWidget(WorkstationProcess process) {
        description = new UILabel();
        if (process instanceof DescribeProcess) {
            DescribeProcess describeProcess = (DescribeProcess) process;
            description.setText(describeProcess.getInputDescription() + " = " + describeProcess.getOutputDescription());
        } else {
            description.setText(process.getId() + " cannot be displayed");
        }
    }


    @Override
    public void onDraw(Canvas canvas) {
        description.onDraw(canvas);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return description.getPreferredContentSize(canvas, sizeHint);
    }
}
