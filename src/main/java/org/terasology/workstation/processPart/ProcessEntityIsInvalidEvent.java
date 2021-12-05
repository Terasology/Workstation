// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.processPart;

import com.google.common.collect.Lists;
import org.terasology.gestalt.entitysystem.event.Event;

import java.util.List;

/**
 * Consume this event if the process is not valid even before it is used in a workstation.
 */
public class ProcessEntityIsInvalidEvent implements Event {
    List<String> errorMessages = Lists.newArrayList();

    public void addError(String message) {
        errorMessages.add(message);
    }

    public Iterable<String> getErrors() {
        return errorMessages;
    }

    public boolean hasErrors() {
        return errorMessages.size() > 0;
    }
}
