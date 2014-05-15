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
package org.terasology.workstation.process;

import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.widgets.UILabel;

public class ProcessPartDescription {
    UIWidget widget;
    String description;

    public ProcessPartDescription(String description) {
        this.description = description;
    }

    public ProcessPartDescription(String description, UIWidget widget) {
        this.widget = widget;
        this.description = description;
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
}

