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

import org.terasology.asset.Assets;
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
        if (process instanceof DescribeProcess) {
            DescribeProcess describeProcess = (DescribeProcess) process;
            ProcessPartDescription inputDesc = describeProcess.getInputDescription();
            flowLayout.addWidget(inputDesc.getWidget(), null);

            UIImage eq = new UIImage();
            eq.setImage(Assets.getTextureRegion("workstation:equals").get());
            flowLayout.addWidget(eq, null);

            ProcessPartDescription outputDesc = describeProcess.getOutputDescription();
            flowLayout.addWidget(outputDesc.getWidget(), null);

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
