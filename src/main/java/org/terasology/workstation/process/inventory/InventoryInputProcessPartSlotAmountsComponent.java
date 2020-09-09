// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process.inventory;

import com.google.common.collect.Maps;
import org.terasology.engine.entitySystem.Component;

import java.util.Map;

public class InventoryInputProcessPartSlotAmountsComponent implements Component {
    public Map<Integer, Integer> slotAmounts = Maps.newHashMap();


    public InventoryInputProcessPartSlotAmountsComponent() {
    }

    public InventoryInputProcessPartSlotAmountsComponent(Map<Integer, Integer> slotAmounts) {
        this.slotAmounts = slotAmounts;
    }
}
