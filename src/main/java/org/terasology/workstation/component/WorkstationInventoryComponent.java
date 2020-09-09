// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.component;

import com.google.common.collect.Maps;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.reflection.MappedContainer;

import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@ForceBlockActive
public class WorkstationInventoryComponent implements Component {
    @Replicate
    public Map<String, SlotAssignment> slotAssignments = Maps.newHashMap();

    @MappedContainer
    public static class SlotAssignment {
        public int slotStart;
        public int slotCount;

        public SlotAssignment() {
        }

        public SlotAssignment(int slotStart, int slotCount) {
            this.slotStart = slotStart;
            this.slotCount = slotCount;
        }
    }
}
