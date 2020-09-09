// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.ForceBlockActive;

import java.util.Map;

@ForceBlockActive
public class WorkstationComponent implements Component {
    @Replicate
    public Map<String, Boolean> supportedProcessTypes;
}
