// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.component;

import org.terasology.gestalt.entitysystem.component.Component;

public class ProcessTypeDescriptionComponent implements Component<ProcessTypeDescriptionComponent> {
    public String name;

    @Override
    public void copyFrom(ProcessTypeDescriptionComponent other) {
        this.name = other.name;
    }
}
