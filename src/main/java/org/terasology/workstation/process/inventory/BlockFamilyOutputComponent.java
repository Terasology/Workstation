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
