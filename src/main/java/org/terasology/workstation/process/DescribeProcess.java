// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.workstation.process;

import java.util.Collection;

public interface DescribeProcess {
    Collection<ProcessPartDescription> getOutputDescriptions();

    Collection<ProcessPartDescription> getInputDescriptions();
}
