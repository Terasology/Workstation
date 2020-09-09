// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.ui;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.inventory.rendering.nui.layers.ingame.ItemCell;
import org.terasology.nui.databinding.DefaultBinding;
import org.terasology.nui.widgets.TooltipLine;
import org.terasology.nui.widgets.UIList;

public class InventoryItem extends ItemCell {

    EntityRef item;

    public InventoryItem(EntityRef item) {

        this.item = item;

        // make a copy of all the values so that the item can be garbaged
        icon.bindIcon(new DefaultBinding(icon.getIcon()));
        icon.bindMesh(new DefaultBinding(icon.getMesh()));
        icon.bindQuantity(new DefaultBinding(icon.getQuantity()));
        UIList<TooltipLine> tooltip = (UIList<TooltipLine>) icon.getTooltip();
        if (tooltip != null) {
            tooltip.bindList(new DefaultBinding(tooltip.getList()));
        }

        this.item = EntityRef.NULL;
    }


    @Override
    public EntityRef getTargetItem() {
        return item;
    }
}
