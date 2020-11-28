// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process;

import org.terasology.assets.ResourceUrn;
import org.terasology.nui.UIWidget;
import org.terasology.nui.widgets.UILabel;

public class ProcessPartDescription {
    UIWidget widget;

    String displayName;
    ResourceUrn resourceUrn;

    public ProcessPartDescription(ResourceUrn resourceUrn, String displayName) {
        this(resourceUrn, displayName, new UILabel(displayName));
    }

    public ProcessPartDescription(ResourceUrn resourceUrn, String displayName, UIWidget widget) {
        this.resourceUrn = resourceUrn;
        this.displayName = displayName;
        this.widget = widget;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UIWidget getWidget() {
        if (widget == null) {
            UILabel textDesc = new UILabel();
            textDesc.setText(displayName);
            return textDesc;
        } else {
            return widget;
        }
    }

    public ResourceUrn getResourceUrn() {
        return resourceUrn;
    }
}

