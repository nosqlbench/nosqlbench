/*
 * Copyright (c) 2024 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.nb.api.components.status;

import io.nosqlbench.nb.api.components.core.NBInvokableState;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.RepresentToNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.representer.StandardRepresenter;

public class HeartbeatRepresenter extends StandardRepresenter {
    public HeartbeatRepresenter(DumpSettings settings) {
        super(settings);
        this.representers.put(NBInvokableState.class, new RepresentEnumToString());
    }

    public class RepresentEnumToString implements RepresentToNode {

        @Override
        public Node representData(Object o) {
            if (o instanceof Enum<?> e) {
                String name = e.name();
                return HeartbeatRepresenter.this.represent(name);
            } else {
                throw new RuntimeException("Unable to represent as enum: " + o.toString() + " (class " + o.getClass().getSimpleName() + "'");
            }
        }
    }
}
