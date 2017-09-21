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
package org.terasology.workstation.component;

import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;
import org.terasology.world.block.ForceBlockActive;

import java.util.Map;

/**
 * This component stores information about all the process types this workstation supports. Specifically, this contains
 * the names, max supported levels, and whether the process type is automated by this workstation.
 */
@ForceBlockActive
public class WorkstationComponent implements Component {
    /**
     * This map stores all the processes that this workstation supports. The String (key) stores the process type name,
     * while the WorkstationProcessType (key) stores the name/type and max supported level of the process, as well as
     * whether this workstation automatically (true) or manually (false) performs the process.
     */
    @Replicate
    //public Map<WorkstationProcessType, Boolean> supportedProcessTypes;
    public Map<String, WorkstationProcessType> supportedProcessTypes;
}
