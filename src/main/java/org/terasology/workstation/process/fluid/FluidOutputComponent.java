// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process.fluid;

import com.google.common.collect.Maps;
import org.terasology.engine.entitySystem.Component;

import java.util.Map;

public class FluidOutputComponent implements Component {
    public Map<String, Float> fluidVolumes = Maps.newHashMap();
}
