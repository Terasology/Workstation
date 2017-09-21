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
package org.terasology.workstation.system;

import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.registry.In;
import org.terasology.registry.InjectionHelper;
import org.terasology.registry.Share;
import org.terasology.workstation.component.ProcessDefinitionComponent;
import org.terasology.workstation.component.WorkstationProcessType;
import org.terasology.workstation.process.WorkstationProcess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of a workstation registry to globally store all of the workstation process type names and
 * workstation processes a game may have.
 */
@RegisterSystem
@Share(WorkstationRegistry.class)
public class WorkstationRegistryImpl extends BaseComponentSystem implements WorkstationRegistry {
    @In
    PrefabManager prefabManager;

    /** A set of all process types that have been scanned in. */
    private Set<String> scannedTypes = new HashSet<>();

    /**
     * Outer string (key) is the processType like "BasicWoodcraftingProcess" or "BasicSmithingProcess".
     * Inner string (key) is the ID for a WorkstationProcess.
     * Inner WorkstationProcess (value) is the process for making an item (or similar). These tend to be the recipes for
     * making items(?).
     */
    private Map<String, Map<String, WorkstationProcess>> workstationProcesses = new LinkedHashMap<>();

    /**
     * Register all processes of a particular process type using the provided workstation process factory. The processes
     * will be registered into the workstationProcesses map.
     *
     * @param processType   The name of the process type. For example, "BasicWoodcraftingProcess".
     * @param factory       The factory that will be used to create all the individual processes.
     */
    @Override
    public void registerProcessFactory(String processType, WorkstationProcessFactory factory) {
        registerProcesses(processType, factory);
    }

    /**
     * Return a collection of all the workstation processes that have the same workstation process type names as the
     * ones provided. Note that this will not check to see if the level requirements are met.
     *
     * @param processTypes  A collection of Strings that contains the process type names being searched for.
     * @return              A collection of workstation processes that match the process type names of the inputted
     *                      processTypes.
     */
    @Override
    public Collection<WorkstationProcess> getWorkstationProcesses(Collection<String> processTypes) {
        Map<String, WorkstationProcess> processes = new LinkedHashMap<>();
        for (String processType : processTypes) {
            if (!scannedTypes.contains(processType)) {
                registerProcesses(processType, new ProcessPartWorkstationProcessFactory());
            }
            processes.putAll(workstationProcesses.get(processType));
        }

        return processes.values();
    }

    /**
     * Return a collection of all the workstation processes that are of the provided WorkstationProcessTypes. Note that
     * this will not check to see if the level requirements are met.
     *
     * @param processTypes  The ArrayList of WorkstationProcessTypes that contains the supported process names and
     *                      supported process levels or tiers. This is intended to be taken from a WorkstationComponent
     *                      or similar class.
     * @return              A collection of workstation processes that match the process name values of the given
     *                      WorkstationProcessTypes.
     */
    @Override
    public Collection<WorkstationProcess> getWorkstationProcesses(ArrayList<WorkstationProcessType> processTypes) {
        Map<String, WorkstationProcess> processes = new LinkedHashMap<>();

        for (int i = 0; i < processTypes.size(); i++) {
            String processName = processTypes.get(i).processName;

            if (!scannedTypes.contains(processName)) {
                registerProcesses(processName, new ProcessPartWorkstationProcessFactory());
            }
            processes.putAll(workstationProcesses.get(processName));
        }

        return processes.values();
    }

    /**
     * Return a collection of all the workstation processes that are of the provided WorkstationProcessTypes, and have a
     * process level that's lower than or equal to the ones provided in the processTypes argument.
     *
     * @param processTypes  The ArrayList of WorkstationProcessTypes that contains the supported process type names and
     *                      supported process levels or tiers. This is intended to be taken from a WorkstationComponent
     *                      or similar class.
     * @return              A collection of workstation processes that match the parameters of the given
     *                      WorkstationProcessTypes. That is to say, they have the same process name, and are in the
     *                      appropriate process level range.
     */
    @Override
    public Collection<WorkstationProcess> getWorkstationProcessesByLevel(ArrayList<WorkstationProcessType> processTypes) {
        Map<String, WorkstationProcess> processes = new LinkedHashMap<>();

        for (int i = 0; i < processTypes.size(); i++) {
            String processName = processTypes.get(i).processName;

            if (!scannedTypes.contains(processName)) {
                registerProcesses(processName, new ProcessPartWorkstationProcessFactory());
            }

            for (Map.Entry<String, WorkstationProcess> entry : workstationProcesses.get(processName).entrySet()) {
                // Make sure that the workstation actually supports this process level, before adding it to the
                // processes map.
                if (processTypes.get(i).processLevel >= entry.getValue().getProcessLevel()) {
                    processes.put(entry.getKey(), entry.getValue());
                }
            }
        }

        return processes.values();
    }

    /**
     * Register the provided workstation process with a particular process type into the workstationProcesses map.
     *
     * @param processType           The name of the process type. For example, "BasicWoodcraftingProcess".
     * @param workstationProcess    The workstation process to be registered.
     */
    @Override
    public void registerProcess(String processType, WorkstationProcess workstationProcess) {
        Map<String, WorkstationProcess> processes = workstationProcesses.get(processType);
        if (processes == null) {
            processes = new HashMap<>();
            workstationProcesses.put(processType, processes);
        }
        processes.put(workstationProcess.getId(), workstationProcess);
    }

    /**
     * Register the provided workstation process with a particular process type into the workstationProcesses map.
     * TODO: Could we just use the regular registerProcesses(String, WorkstationProcess) function?
     * TODO: Could we just use the processType present in the workstationProcess itself, and remove the first arg?
     * TODO: Should there be an extra check to ensure that the workstationProcess's processType is the same as the processType arg?
     *
     * @param processType           The name of the process type. For example, "BasicWoodcraftingProcess".
     * @param processLevel          The level of the process. Likely unneeded.
     * @param workstationProcess    The workstation process to be registered.
     */
    @Override
    public void registerProcess(String processType, int processLevel, WorkstationProcess workstationProcess) {
        Map<String, WorkstationProcess> processes = workstationProcesses.get(processType);
        if (processes == null) {
            processes = new HashMap<>();
            workstationProcesses.put(processType, processes);
        }
        processes.put(workstationProcess.getId(), workstationProcess);
    }

    /**
     * Find a workstation process that's been registered into this registry using a collection of possible process
     * types as well as its processId.
     *
     * @param supportedProcessTypes A collection of Strings that contains the names of the process types being searched
     *                              for.
     * @param processId             The ID of the process.
     * @return                      The workstation process that has a matching process type as well as process ID. If
     *                              it doesn't exist, null is returned.
     */
    @Override
    public WorkstationProcess getWorkstationProcessById(Collection<String> supportedProcessTypes, String processId) {
        for (WorkstationProcess workstationProcess : getWorkstationProcesses(supportedProcessTypes)) {
            if (workstationProcess.getId().equals(processId)) {
                return workstationProcess;
            }
        }
        return null;
    }

    /**
     * Find a workstation process that's been registered into this registry using an ArrayList of supported
     * WorkstationProcessTypes (type names and max supported levels) as well as its processId. Unlike the
     * {@link #getWorkstationProcessById(Collection, String)} method, this will also check to see if the process
     * qualifies under the max level requirements.
     *
     * @param supportedProcessTypes The ArrayList of WorkstationProcessTypes that contains information on the supported
     *                              process types. This is intended to be taken from a WorkstationComponent or similar
     *                              class.
     * @param processId             The ID of the process.
     * @return                      The workstation process that has a matching process type, fits the level
     *                              requirements, and has the same process ID. If it doesn't exist, null is returned.
     */
    public WorkstationProcess getWorkstationProcessByIdAndLevel(ArrayList<WorkstationProcessType> supportedProcessTypes, String processId) {
        for (WorkstationProcess workstationProcess : getWorkstationProcessesByLevel(supportedProcessTypes)) {
            if (workstationProcess.getId().equals(processId)) {
                return workstationProcess;
            }
        }
        return null;
    }

    /**
     * Find and register all processes of a particular process type using the provided workstation process factory.
     * The processes will be registered into the workstationProcesses map, and each process type will be added to the
     * scannedTypes HashSet.
     *
     * @param processType   The name of the process type. For example, "BasicWoodcraftingProcess".
     * @param factory       The factory that will be used to create all the individual processes.
     */
    private void registerProcesses(String processType, WorkstationProcessFactory factory) {
        InjectionHelper.inject(factory);
        Map<String, WorkstationProcess> processes = new HashMap<>();
        if (workstationProcesses.containsKey(processType)) {
            processes.putAll(workstationProcesses.get(processType));
        }

        for (Prefab prefab : prefabManager.listPrefabs(ProcessDefinitionComponent.class)) {
            ProcessDefinitionComponent processDef = prefab.getComponent(ProcessDefinitionComponent.class);
            if (processDef.processType.equals(processType)) {
                WorkstationProcess process = factory.createProcess(prefab);
                if (process != null) {
                    processes.put(process.getId(), process);
                }
            }
        }
        workstationProcesses.put(processType, processes);
        scannedTypes.add(processType);
    }

    @Command(shortDescription = "Reset all workstation processes")
    public String resetWorkstationProcesses() {
        scannedTypes.clear();
        workstationProcesses.clear();
        return "All known processes cleared";
    }

}
