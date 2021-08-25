// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process.fluid;

import com.google.common.collect.Maps;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Map;

public class FluidOutputComponent implements Component<FluidOutputComponent> {
    public Map<String, Float> fluidVolumes = Maps.newHashMap();

    @Override
    public void copyFrom(FluidOutputComponent other) {
        this.fluidVolumes = Maps.newHashMap(other.fluidVolumes);
    }
}
