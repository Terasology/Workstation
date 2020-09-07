// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.nui.UIWidget;
import org.terasology.nui.widgets.UILabel;

public class ProcessPartDescription {
    UIWidget widget;
    String description;
    ResourceUrn resourceUrn;

    public ProcessPartDescription(ResourceUrn resourceUrn, String description) {
        this(resourceUrn, description, new UILabel(description));
    }

    public ProcessPartDescription(ResourceUrn resourceUrn, String description, UIWidget widget) {
        this.resourceUrn = resourceUrn;
        this.description = description;
        this.widget = widget;
    }

    @Override
    public String toString() {
        return description;
    }

    public UIWidget getWidget() {
        if (widget == null) {
            UILabel textDesc = new UILabel();
            textDesc.setText(description);
            return textDesc;
        } else {
            return widget;
        }
    }

    public ResourceUrn getResourceUrn() {
        return resourceUrn;
    }
}

