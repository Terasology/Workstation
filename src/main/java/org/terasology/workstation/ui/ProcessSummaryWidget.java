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

import org.terasology.utilities.Assets;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.layouts.FlowLayout;
import org.terasology.rendering.nui.widgets.UIImage;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.workstation.process.DescribeProcess;
import org.terasology.workstation.process.ProcessPartDescription;
import org.terasology.workstation.process.WorkstationProcess;

/**
 * Displays the input and output of a process
 */
public class ProcessSummaryWidget extends CoreWidget {
    UIWidget widget;

    public ProcessSummaryWidget(WorkstationProcess process) {
        FlowLayout flowLayout = new FlowLayout();
        UIImage plus = new UIImage(Assets.getTextureRegion("workstation:plus").get());
        UIImage eq = new UIImage(Assets.getTextureRegion("workstation:equals").get());

        if (process instanceof DescribeProcess) {
            DescribeProcess describeProcess = (DescribeProcess) process;
            boolean isFirst = true;
            // add all input widgets
            for (ProcessPartDescription inputDesc : describeProcess.getInputDescriptions()) {
                if (!isFirst) {
                    flowLayout.addWidget(plus, null);
                }
                isFirst = false;
                flowLayout.addWidget(inputDesc.getWidget(), null);
            }

            // add the equals separator
            flowLayout.addWidget(eq, null);

            // add the output widgets
            isFirst = true;
            for (ProcessPartDescription outputDesc : describeProcess.getOutputDescriptions()) {
                if (!isFirst) {
                    flowLayout.addWidget(plus, null);
                }
                isFirst = false;
                flowLayout.addWidget(outputDesc.getWidget(), null);
            }

            widget = flowLayout;
        } else {
            UILabel errorLabel = new UILabel();
            errorLabel.setText(process.getId() + " cannot be displayed");
            widget = errorLabel;
        }
    }


    @Override
    public void onDraw(Canvas canvas) {
        widget.onDraw(canvas);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return widget.getPreferredContentSize(canvas, sizeHint);
    }
}
