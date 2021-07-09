// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.component;

import org.terasology.gestalt.entitysystem.component.Component;

public class SpecificInputSlotComponent implements Component<SpecificInputSlotComponent> {
    public int slot;

    public SpecificInputSlotComponent() {
    }

    public SpecificInputSlotComponent(int slot) {
        this.slot = slot;
    }

    @Override
    public void copy(SpecificInputSlotComponent other) {
        this.slot = other.slot;
    }
}
