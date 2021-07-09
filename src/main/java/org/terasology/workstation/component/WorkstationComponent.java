// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.component;

import com.google.common.collect.Maps;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Map;

@ForceBlockActive
public class WorkstationComponent implements Component<WorkstationComponent> {
    @Replicate
    public Map<String, Boolean> supportedProcessTypes;

    @Override
    public void copy(WorkstationComponent other) {
        this.supportedProcessTypes = Maps.newHashMap(other.supportedProcessTypes);
    }
}
