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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.registry.CoreRegistry;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ItemPrefabOutputComponent extends InventoryOutputComponent {
    private static final Logger logger = LoggerFactory.getLogger(ItemPrefabOutputComponent.class);

    public Map<String, Integer> itemCounts;

    @Override
    protected Set<EntityRef> createOutputItems(EntityRef processEntity) {
        return createOutputItems(itemCounts, processEntity.isPersistent());
    }

    public static Set<EntityRef> createOutputItems(Map<String, Integer> itemCounts, boolean createPersistentEntities) {
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);

        Set<EntityRef> result = new HashSet<>();
        for (Map.Entry<String, Integer> itemCount : itemCounts.entrySet()) {
            try {
                EntityBuilder entityBuilder = entityManager.newBuilder(itemCount.getKey());
                entityBuilder.setPersistent(createPersistentEntities);
                ItemComponent item = entityBuilder.getComponent(ItemComponent.class);
                item.stackCount = itemCount.getValue().byteValue();
                entityBuilder.saveComponent(item);
                result.add(entityBuilder.build());
            } catch (Exception ex) {
                logger.error("Bad block: " + itemCount.getKey());
                throw ex;
            }
        }

        return result;
    }
}
