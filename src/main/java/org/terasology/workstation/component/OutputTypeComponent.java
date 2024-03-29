// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.component;

import org.terasology.gestalt.entitysystem.component.Component;

public class OutputTypeComponent implements Component<OutputTypeComponent> {
    public String type;

    public OutputTypeComponent() {
    }

    public OutputTypeComponent(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(OutputTypeComponent other) {
        this.type = other.type;
    }
}
