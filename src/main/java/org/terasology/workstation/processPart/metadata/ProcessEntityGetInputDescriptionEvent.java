// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.processPart.metadata;

import com.google.common.collect.Lists;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.workstation.process.ProcessPartDescription;

import java.util.List;

/**
 * Add input descriptions of the process without relying on validation
 */
public class ProcessEntityGetInputDescriptionEvent implements Event {
    List<ProcessPartDescription> inputDescriptions = Lists.newLinkedList();

    public void addInputDescription(ProcessPartDescription processPartDescription) {
        inputDescriptions.add(processPartDescription);
    }

    public Iterable<ProcessPartDescription> getInputDescriptions() {
        return inputDescriptions;
    }
}
