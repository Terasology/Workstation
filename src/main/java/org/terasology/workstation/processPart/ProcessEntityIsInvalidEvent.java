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
