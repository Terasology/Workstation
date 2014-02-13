package org.terasology.workstation.component;

import org.terasology.entitySystem.Component;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class WorkstationInventoryComponent implements Component {
    public List<Integer> inputSlots;
    public List<Integer> outputSlots;
}
