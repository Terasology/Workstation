/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.workstation.processPart.metadata;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.event.Event;
import org.terasology.workstation.process.ProcessPartDescription;

import java.util.List;

/**
 * Add output descriptions of the process without relying on validation
 */
public class ProcessEntityGetOutputDescriptionEvent implements Event {
    List<ProcessPartDescription> outputDescriptions = Lists.newLinkedList();

    public void addOutputDescription(ProcessPartDescription processPartDescription) {
        outputDescriptions.add(processPartDescription);
    }

    public Iterable<ProcessPartDescription> getOutputDescriptions() {
        return outputDescriptions;
    }
}
