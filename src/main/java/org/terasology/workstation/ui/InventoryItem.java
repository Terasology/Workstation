/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.workstation.ui;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.layers.ingame.inventory.ItemCell;
import org.terasology.rendering.nui.widgets.TooltipLine;
import org.terasology.rendering.nui.widgets.UIList;

public class InventoryItem extends ItemCell {

    EntityRef item;

    public InventoryItem(EntityRef item) {

        this.item = item;

        // make a copy of all the values so that the item can be garbaged
        icon.bindIcon(new DefaultBinding(icon.getIcon()));
        icon.bindMesh(new DefaultBinding(icon.getMesh()));
        icon.bindQuantity(new DefaultBinding(icon.getQuantity()));
        ((UIList<TooltipLine>) icon.getTooltip()).bindList(new DefaultBinding(((UIList<TooltipLine>) icon.getTooltip()).getList()));
    }


    @Override
    public EntityRef getTargetItem() {
        return item;
    }
}
