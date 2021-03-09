/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
