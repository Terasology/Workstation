// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process;

import org.terasology.engine.entitySystem.entity.EntityRef;

public interface ValidateProcess {
    boolean isValid(EntityRef instigator, EntityRef workstation);
}
