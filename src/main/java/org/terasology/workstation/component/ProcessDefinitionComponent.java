// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.component;

import org.terasology.gestalt.entitysystem.component.Component;

public class ProcessDefinitionComponent implements Component<ProcessDefinitionComponent> {
    public String processType;

    @Override
    public void copy(ProcessDefinitionComponent other) {
        this.processType = other.processType;
    }
}
