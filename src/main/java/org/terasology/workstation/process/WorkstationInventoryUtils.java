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

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.InventoryAccessComponent;
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

    public static List<Integer> getAssignedSlots(EntityRef workstation, boolean isOutputType, String type) {
        if (isOutputType) {
            return getAssignedOutputSlots(workstation, type);
        } else {
            return getAssignedInputSlots(workstation, type);
        }
    }

    public static List<Integer> getAssignedInputSlots(EntityRef workstation, String type) {
        InventoryAccessComponent inventoryAccessComponent = workstation.getComponent(InventoryAccessComponent.class);
        if (inventoryAccessComponent != null && inventoryAccessComponent.input.containsKey(type)) {
            return Lists.newLinkedList(inventoryAccessComponent.input.get(type));
        }
        // fallback to the old system
        return getAssignedSlots(workstation, type);
    }

    public static List<Integer> getAssignedOutputSlots(EntityRef workstation, String type) {
        InventoryAccessComponent inventoryAccessComponent = workstation.getComponent(InventoryAccessComponent.class);
        if (inventoryAccessComponent != null && inventoryAccessComponent.output.containsKey(type)) {
            return Lists.newLinkedList(inventoryAccessComponent.output.get(type));
        }
        // fallback to the old system
        return getAssignedSlots(workstation, type);
    }

    public static boolean hasAssignedSlots(EntityRef workstation, boolean isOutputCategory, String type) {
        InventoryAccessComponent inventoryAccessComponent = workstation.getComponent(InventoryAccessComponent.class);
        if (inventoryAccessComponent != null) {
            if (isOutputCategory) {
                return inventoryAccessComponent.output.containsKey(type);
            } else {
                return inventoryAccessComponent.input.containsKey(type);
            }
        }
        return false;
    }
}
