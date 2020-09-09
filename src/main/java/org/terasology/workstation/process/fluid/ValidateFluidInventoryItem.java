// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process.fluid;

import org.terasology.engine.entitySystem.entity.EntityRef;

public interface ValidateFluidInventoryItem {
    boolean isValidFluid(EntityRef workstation, int slotNo, EntityRef instigator, String fluidType);
}
