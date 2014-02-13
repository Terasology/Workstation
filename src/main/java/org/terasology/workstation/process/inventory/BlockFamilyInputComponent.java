package org.terasology.workstation.process.inventory;

import com.google.common.base.Predicate;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.items.BlockItemComponent;

import java.util.HashMap;
import java.util.Map;

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

    private final static class BlockFamilyPredicate implements Predicate<EntityRef> {
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
