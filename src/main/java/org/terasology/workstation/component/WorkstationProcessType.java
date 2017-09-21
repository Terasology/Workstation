/*
 * Copyright 2017 MovingBlocks
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

import org.terasology.network.Replicate;
import org.terasology.reflection.MappedContainer;

/**
 * This class stores information about a workstation process type. That is, a type or class of processes that can be
 * performed at the appropriate workstations.
 */
@MappedContainer
@Replicate
public class WorkstationProcessType {
    /** Name of the process type. */
    @Replicate
    public String processName;

    /** The maximum level of this process type that this workstation supports up to. */
    @Replicate
    public int processLevel;

    /** Indicates whether this process type can be performed automatically or not. */
    @Replicate
    public boolean isAutomatic;
}
