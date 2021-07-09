// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process.inventory;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.Owns;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;

public class InventoryInputItemsComponent implements Component<InventoryInputItemsComponent> {
    @Owns
    public List<EntityRef> items = Lists.newArrayList();

    public InventoryInputItemsComponent() {
    }

    public InventoryInputItemsComponent(Iterable<EntityRef> items) {
        this.items = Lists.newArrayList(items);
    }

    @Override
    public void copy(InventoryInputItemsComponent other) {
        this.items = Lists.newArrayList(other.items);
    }
}
