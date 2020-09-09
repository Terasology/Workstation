// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.component;

import org.terasology.engine.entitySystem.Component;

public class SpecificInputSlotComponent implements Component {
    public int slot;

    public SpecificInputSlotComponent() {
    }

    public SpecificInputSlotComponent(int slot) {
        this.slot = slot;
    }
}
