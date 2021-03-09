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
