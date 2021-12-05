// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.ui;

import org.joml.Vector2i;
import org.terasology.engine.utilities.Assets;
import org.terasology.nui.Canvas;
import org.terasology.nui.CoreWidget;
import org.terasology.nui.UIWidget;
import org.terasology.nui.layouts.FlowLayout;
import org.terasology.nui.widgets.UIImage;
import org.terasology.nui.widgets.UILabel;
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
