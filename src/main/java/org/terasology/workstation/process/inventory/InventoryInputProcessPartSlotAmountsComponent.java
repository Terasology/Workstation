// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process.inventory;

import com.google.common.collect.Maps;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Map;

public class InventoryInputProcessPartSlotAmountsComponent implements Component<InventoryInputProcessPartSlotAmountsComponent> {
    public Map<Integer, Integer> slotAmounts = Maps.newHashMap();


    public InventoryInputProcessPartSlotAmountsComponent() {
    }

    public InventoryInputProcessPartSlotAmountsComponent(Map<Integer, Integer> slotAmounts) {
        this.slotAmounts = slotAmounts;
    }

    @Override
    public void copy(InventoryInputProcessPartSlotAmountsComponent other) {
        this.slotAmounts = Maps.newHashMap(other.slotAmounts);
    }
}
