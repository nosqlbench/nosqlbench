/*
 * Copyright (c) nosqlbench
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

import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.labels.NBLabels;

import java.util.Map;
import java.util.Optional;

/**
 * A <EM>heartbeat</EM> component is one which provides evidence that it is either
 * in a healthy state or that it is not, via a heartbeat mechanism. This requires
 * that a component property 'heartbeat' is provides which is the millisecond interval
 * between beats.
 */
public class NBHeartbeatComponent extends NBStatusComponent {

    public NBHeartbeatComponent(NBComponent parentComponent) {
        super(parentComponent);
    }

    public NBHeartbeatComponent(NBComponent parentComponent, NBLabels componentSpecificLabelsOnly, Map<String, String> props, String liveLabel) {
        super(parentComponent, componentSpecificLabelsOnly, props);
        getComponentProp("heartbeat")
            .map(Long::parseLong)
            .ifPresent(
                // attaches, no further reference needed
                hbmillis -> new ComponentPulse(this, NBLabels.forKV(), liveLabel, hbmillis)
            );
    }

}
