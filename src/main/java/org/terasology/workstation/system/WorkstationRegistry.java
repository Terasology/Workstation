// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.system;

import org.terasology.workstation.process.WorkstationProcess;

import java.util.Collection;

public interface WorkstationRegistry {
    void registerProcessFactory(String processType, WorkstationProcessFactory factory);

    void registerProcess(String processType, WorkstationProcess workstationProcess);

    Collection<WorkstationProcess> getWorkstationProcesses(Collection<String> processType);

    WorkstationProcess getWorkstationProcessById(Collection<String> supportedProcessTypes, String processId);
}
