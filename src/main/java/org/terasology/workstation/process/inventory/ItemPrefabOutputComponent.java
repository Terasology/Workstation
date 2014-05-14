package org.terasology.workstation.process.inventory;

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
    public Map<String, Integer> itemCounts;

    @Override
    protected Set<EntityRef> createOutputItems() {
        return createOutputItems(itemCounts);
    }

    public static Set<EntityRef> createOutputItems(Map<String, Integer> itemCounts) {
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);

        Set<EntityRef> result = new HashSet<>();
        for (Map.Entry<String, Integer> itemCount : itemCounts.entrySet()) {
            EntityRef entityRef = entityManager.create(itemCount.getKey());
            ItemComponent item = entityRef.getComponent(ItemComponent.class);
            item.stackCount = itemCount.getValue().byteValue();
            entityRef.saveComponent(item);

            result.add(entityRef);
        }

        return result;
    }
}
