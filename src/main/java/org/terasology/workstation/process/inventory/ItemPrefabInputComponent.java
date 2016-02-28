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
package org.terasology.workstation.process.inventory;

import com.google.common.base.Predicate;
import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.ItemComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ItemPrefabInputComponent extends InventoryInputComponent {
    public Map<String, Integer> itemCounts;

    @Override
    protected Map<Predicate<EntityRef>, Integer> getInputItems() {
        Map<Predicate<EntityRef>, Integer> result = new HashMap<>();
        for (Map.Entry<String, Integer> itemCount : itemCounts.entrySet()) {
            result.put(new ItemPrefabPredicate(Assets.getPrefab(itemCount.getKey()).get().getUrn()), itemCount.getValue());
        }

        return result;
    }

    @Override
    protected Set<EntityRef> createItems() {
        return ItemPrefabOutputComponent.createOutputItems(itemCounts, false);
    }

    private static final class ItemPrefabPredicate implements Predicate<EntityRef> {
        private ResourceUrn prefab;

        private ItemPrefabPredicate(ResourceUrn prefab) {
            this.prefab = prefab;
        }

        @Override
        public boolean apply(EntityRef input) {
            ItemComponent item = input.getComponent(ItemComponent.class);
            if (item == null) {
                return false;
            }
            return input.getParentPrefab().getUrn().equals(prefab);
        }
    }
}
