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
package org.terasology.workstation.process.inventory;

import com.google.common.collect.Sets;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;

import java.util.Set;


public class InventoryOutputItemsComponent implements Component {
    Set<EntityRef> outputItems = Sets.newHashSet();

    public InventoryOutputItemsComponent() {
    }

    public InventoryOutputItemsComponent(Set<EntityRef> outputItems) {
        this.outputItems = outputItems;
    }

    public Set<EntityRef> getOutputItems() {
        return outputItems;
    }
}
