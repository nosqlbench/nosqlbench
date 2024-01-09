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
import io.nosqlbench.nb.api.labels.NBLabels;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;

import java.util.Map;

public record Heartbeat(
        NBLabels labels,
        NBInvokableState state,
        long started_at,
        long session_time_ns,
        long heartbeat_interval_ms,
        long heartbeat_epoch_ms
) {
    public final static Dump dump = createDump();

    private static Dump createDump() {

        DumpSettings settings = DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK).build();
        return new Dump(settings, new HeartbeatRepresenter(settings));
    }

    public Heartbeat withHeartbeatDetails(long new_heartbeat_interval_ms, long new_heartbeat_ms_epoch) {
        return new Heartbeat(
                labels,
                state,
                started_at,
                session_time_ns,
                new_heartbeat_interval_ms,
                new_heartbeat_ms_epoch
        );
    }

    public String toYaml() {
        return toString();
    }

    public Map<String, Object> toMap() {
        return Map.of(
                "labels", labels.asMap(),
                "state", state,
                "started_at_epochms", started_at,
                "session_time_ns", session_time_ns,
                "heartbeat_interval_ms", heartbeat_interval_ms,
                "heartbeat_epoch_ms", heartbeat_epoch_ms
        );
    }

    @Override
    public String toString() {
        return dump.dumpToString(toMap());
    }
}
