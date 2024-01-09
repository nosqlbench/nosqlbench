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

import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.core.NBInvokableState;
import io.nosqlbench.nb.api.labels.NBLabels;

import java.util.Map;

/**
 * A <EM>live</EM> component is one which provides evidence that it is either
 * in a healthy state or that it is not, via a heartbeat mechanism.
 */
public class NBLiveComponent extends NBBaseComponent {

    public NBLiveComponent(NBComponent parentComponent) {
        super(parentComponent);
    }

    public NBLiveComponent(NBComponent parentComponent, NBLabels componentSpecificLabelsOnly, Map<String, String> props, String liveLabel) {
        super(parentComponent, componentSpecificLabelsOnly, props);
        // attaches, no further reference needed
        new ComponentPulse(this, NBLabels.forKV(), liveLabel, Long.parseLong(getComponentProp("heartbeat").orElse("60000")));
    }

    public Heartbeat heartbeat() {
        return new Heartbeat(
            getLabels(),
            this.getComponentState(),
            started_ns,
            sessionTimeMs(),
            0L,
            0L
        );
    }

    private long sessionTimeMs() {
        NBInvokableState state = getComponentState();
        long nanos = switch (state) {
            case ERRORED -> (nanosof_error() - nanosof_start());
            case STARTING, RUNNING -> (System.nanoTime() - nanosof_start());
            case CLOSING -> (nanosof_close() - nanosof_start());
            case STOPPED -> (nanosof_teardown() - nanosof_start());
        };
        return nanos / 1_000_000L;
    }
}
