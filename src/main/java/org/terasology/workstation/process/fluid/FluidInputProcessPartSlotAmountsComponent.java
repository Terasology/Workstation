// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process.fluid;

import com.google.common.collect.Maps;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Map;

public class FluidInputProcessPartSlotAmountsComponent implements Component<FluidInputProcessPartSlotAmountsComponent> {
    public Map<Integer, Float> slotAmounts = Maps.newHashMap();

    public FluidInputProcessPartSlotAmountsComponent() {
    }

    public FluidInputProcessPartSlotAmountsComponent(Map<Integer, Float> slotAmounts) {
        this.slotAmounts = slotAmounts;
    }

    @Override
    public void copyFrom(FluidInputProcessPartSlotAmountsComponent other) {
        this.slotAmounts = Maps.newHashMap(other.slotAmounts);
    }
}
