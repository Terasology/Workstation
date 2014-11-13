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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BlockFamilyOutputComponent extends InventoryOutputComponent {
    public Map<String, Integer> blockCounts;

    @Override
    protected Set<EntityRef> createOutputItems() {
        return createOutputItems(blockCounts);
    }

    public static Set<EntityRef> createOutputItems(Map<String, Integer> blockCounts) {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);

        BlockItemFactory itemFactory = new BlockItemFactory(entityManager);

        Set<EntityRef> result = new HashSet<>();
        for (Map.Entry<String, Integer> blockCount : blockCounts.entrySet()) {
            BlockFamily blockFamily = blockManager.getBlockFamily(blockCount.getKey());
            result.add(itemFactory.newInstance(blockFamily, blockCount.getValue()));
        }

        return result;
    }
}
