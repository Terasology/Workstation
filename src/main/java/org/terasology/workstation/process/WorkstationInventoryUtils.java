// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.module.inventory.components.InventoryAccessComponent;
import org.terasology.workstation.component.WorkstationInventoryComponent;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
