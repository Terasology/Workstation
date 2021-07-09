// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.component;

import org.terasology.gestalt.entitysystem.component.Component;

public class ProcessPartExecutionComponent implements Component<ProcessPartExecutionComponent> {
    public String result;

    @Override
    public void copy(ProcessPartExecutionComponent other) {
        this.result = other.result;
    }
}
