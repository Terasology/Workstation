// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.ui;

import org.terasology.engine.entitySystem.entity.EntityRef;

public interface WorkstationUI {
    void initializeWorkstation(EntityRef workstation);
}
