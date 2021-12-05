// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.system;

import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.registry.In;
import org.terasology.workstation.process.InvalidProcessPartException;
import org.terasology.workstation.process.WorkstationProcess;

public class ProcessPartWorkstationProcessFactory implements WorkstationProcessFactory {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ProcessPartWorkstationProcessFactory.class);

    @In
    private EntityManager entityManager;

    @Override
    public WorkstationProcess createProcess(Prefab prefab) {
        try {
            WorkstationProcess process = new ProcessPartWorkstationProcess(prefab, entityManager);
            return process;
        } catch (InvalidProcessPartException ex) {
            logger.warn("Invalid Process: " + prefab.getName() + ". " + ex.getMessage());
            return null;
        }
    }
}
