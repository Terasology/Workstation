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
package org.terasology.workstation.process;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.workstation.component.WorkstationInventoryComponent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public final class WorkstationInventoryUtils {
    private WorkstationInventoryUtils() {
    }

    public static List<Integer> getAssignedSlots(EntityRef workstation, String type) {
        WorkstationInventoryComponent inventory = workstation.getComponent(WorkstationInventoryComponent.class);
        List<Integer> result = new LinkedList<>();
        if (inventory != null) {
            WorkstationInventoryComponent.SlotAssignment slotAssignment = inventory.slotAssignments.get(type);
            for (int i = 0; i < slotAssignment.slotCount; i++) {
                result.add(slotAssignment.slotStart + i);
            }
        }
        return Collections.unmodifiableList(result);
    }
}
