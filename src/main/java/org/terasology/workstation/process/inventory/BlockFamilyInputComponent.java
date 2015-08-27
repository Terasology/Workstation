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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.items.BlockItemComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BlockFamilyInputComponent extends InventoryInputComponent {
    public Map<String, Integer> blockCounts;

    @Override
    protected Map<Predicate<EntityRef>, Integer> getInputItems() {
        Map<Predicate<EntityRef>, Integer> result = new HashMap<>();
        for (Map.Entry<String, Integer> blockFamilyCount : blockCounts.entrySet()) {
            result.put(new BlockFamilyPredicate(new BlockUri(blockFamilyCount.getKey())), blockFamilyCount.getValue());
        }

        return result;
    }

    @Override
    protected Set<EntityRef> createItems() {
        return BlockFamilyOutputComponent.createOutputItems(blockCounts, false);
    }

    private static final class BlockFamilyPredicate implements Predicate<EntityRef> {
        private BlockUri blockFamilyUri;

        private BlockFamilyPredicate(BlockUri blockFamilyUri) {
            this.blockFamilyUri = blockFamilyUri;
        }

        @Override
        public boolean apply(EntityRef input) {
            BlockItemComponent blockItem = input.getComponent(BlockItemComponent.class);
            if (blockItem == null) {
                return false;
            }
            return blockItem.blockFamily.getURI().equals(blockFamilyUri);
        }
    }
}
