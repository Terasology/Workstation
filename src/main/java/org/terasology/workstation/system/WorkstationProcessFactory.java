// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.system;

import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.workstation.process.WorkstationProcess;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface WorkstationProcessFactory {
    WorkstationProcess createProcess(Prefab prefab);
}
