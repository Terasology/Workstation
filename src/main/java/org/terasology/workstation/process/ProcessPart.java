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

public interface ProcessPart {
    /**
     * Validates if this process can be executed, if not InvalidProcessException should be thrown.
     *
     * @param instigator
     * @param workstation
     * @return
     * @throws InvalidProcessException
     */
    public boolean validate(EntityRef instigator, EntityRef workstation, EntityRef readonlyProcessEntity) throws InvalidProcessException;

    /**
     * Returns duration for this process. All the ProcessParts are queried, and the sum of all results becomes
     * the duration of the process. If the total is 0, the process is executed immediately.
     *
     * @param instigator
     * @param workstation
     * @param processEntity
     * @return
     */
    public long getDuration(EntityRef instigator, EntityRef workstation, EntityRef processEntity);

    /**
     * Starts the execution of the process. In this step, for example, all the products, energy and other consumables
     * could be removed from the workstation.
     *
     * @param instigator
     * @param workstation
     * @param processEntity
     */
    public void executeStart(EntityRef instigator, EntityRef workstation, EntityRef processEntity);

    /**
     * Finishes the execution of the process. In this step, for example, all the resulting blocks/items could be
     * placed in the workstation.
     *
     * @param instigator
     * @param workstation
     * @param processEntity
     */
    public void executeEnd(EntityRef instigator, EntityRef workstation, EntityRef processEntity);
}
