// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process.inventory;

import com.google.common.collect.Maps;
import org.terasology.engine.entitySystem.Component;

import java.util.Map;

public class InventoryInputComponent implements Component {
    public Map<String, Integer> blockCounts = Maps.newHashMap();
    public Map<String, Integer> itemCounts = Maps.newHashMap();
}
