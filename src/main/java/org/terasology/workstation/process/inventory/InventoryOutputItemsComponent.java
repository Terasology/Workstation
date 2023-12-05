// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process.inventory;

import com.google.common.collect.Sets;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Set;

public class InventoryOutputItemsComponent implements Component<InventoryOutputItemsComponent> {
    public Set<EntityRef> outputItems = Sets.newHashSet();

    public InventoryOutputItemsComponent() {
    }

    public InventoryOutputItemsComponent(Set<EntityRef> outputItems) {
        this.outputItems = outputItems;
    }

    public Set<EntityRef> getOutputItems() {
        return outputItems;
    }

    @Override
    public void copyFrom(InventoryOutputItemsComponent other) {
        this.outputItems = Sets.newHashSet(other.outputItems);
    }
}
