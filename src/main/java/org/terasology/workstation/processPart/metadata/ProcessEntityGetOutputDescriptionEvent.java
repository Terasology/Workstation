// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.processPart.metadata;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.event.Event;
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
