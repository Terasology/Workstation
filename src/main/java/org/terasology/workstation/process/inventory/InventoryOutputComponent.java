// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process.inventory;

import com.google.common.collect.Maps;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Map;

public class InventoryOutputComponent implements Component<InventoryOutputComponent> {
    public Map<String, Integer> blockCounts = Maps.newHashMap();
    public Map<String, Integer> itemCounts = Maps.newHashMap();

    @Override
    public void copyFrom(InventoryOutputComponent other) {
        this.blockCounts = Maps.newHashMap(other.blockCounts);
        this.itemCounts = Maps.newHashMap(other.itemCounts);
    }
}
