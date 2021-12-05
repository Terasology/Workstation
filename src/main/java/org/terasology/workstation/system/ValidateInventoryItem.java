// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.system;

import org.terasology.engine.entitySystem.entity.EntityRef;

public interface ValidateInventoryItem {
    boolean isValid(EntityRef workstation, int slotNo, EntityRef instigator, EntityRef item);
}
