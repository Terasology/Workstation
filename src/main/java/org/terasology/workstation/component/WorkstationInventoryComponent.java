// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.component;

import com.google.common.collect.Maps;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.reflection.MappedContainer;

import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@ForceBlockActive
public class WorkstationInventoryComponent implements Component<WorkstationInventoryComponent> {
    @Replicate
    public Map<String, SlotAssignment> slotAssignments = Maps.newHashMap();

    @Override
    public void copy(WorkstationInventoryComponent other) {
        this.slotAssignments.clear();
        for (Map.Entry<String, SlotAssignment> entry : other.slotAssignments.entrySet()) {
            SlotAssignment oldSlot = entry.getValue();
            SlotAssignment newSlot = new SlotAssignment();
            newSlot.slotStart = oldSlot.slotStart;
            newSlot.slotCount = oldSlot.slotCount;
            this.slotAssignments.put(entry.getKey(), newSlot);
        }
    }

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
