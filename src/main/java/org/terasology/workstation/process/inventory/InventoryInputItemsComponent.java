// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process.inventory;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.Owns;
import org.terasology.engine.entitySystem.entity.EntityRef;

import java.util.List;

public class InventoryInputItemsComponent implements Component {
    @Owns
    public List<EntityRef> items = Lists.newArrayList();

    public InventoryInputItemsComponent() {
    }

    public InventoryInputItemsComponent(Iterable<EntityRef> items) {
        this.items = Lists.newArrayList(items);
    }
}
