package org.terasology.workstation.process.inventory;

import org.terasology.entitySystem.entity.EntityRef;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface ValidateInventoryItem {
    boolean isResponsibleForSlot(EntityRef workstation, int slotNo);

    boolean isValid(EntityRef workstation, int slotNo, EntityRef instigator, EntityRef item);
}
