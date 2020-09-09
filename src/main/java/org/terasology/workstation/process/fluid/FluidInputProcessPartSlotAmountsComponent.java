// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process.fluid;

import com.google.common.collect.Maps;
import org.terasology.engine.entitySystem.Component;

import java.util.Map;

public class FluidInputProcessPartSlotAmountsComponent implements Component {
    public Map<Integer, Float> slotAmounts = Maps.newHashMap();

    public FluidInputProcessPartSlotAmountsComponent() {
    }

    public FluidInputProcessPartSlotAmountsComponent(Map<Integer, Float> slotAmounts) {
        this.slotAmounts = slotAmounts;
    }
}
