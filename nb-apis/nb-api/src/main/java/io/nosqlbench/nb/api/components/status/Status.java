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

import io.nosqlbench.nb.api.components.core.NBInvokableState;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record Status(
    NBLabels labels,
    NBInvokableState state,
    long started_epoch_ms,
    long session_time_ms,
    long heartbeat_interval_ms,
    long heartbeat_epoch_ms,
    List<Status> substatus
) {
    public final static Dump dump = createDump();

    private static Dump createDump() {
        DumpSettings settings = DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK).build();
        return new Dump(settings, new HeartbeatRepresenter(settings));
    }

    public Status withHeartbeatDetails(long new_heartbeat_interval_ms, long new_heartbeat_ms_epoch) {
        return new Status(
            labels,
            state,
            started_epoch_ms,
            session_time_ms,
            new_heartbeat_interval_ms,
            new_heartbeat_ms_epoch,
            substatus
        );
    }

    public String toYaml() {
        return toString();
    }

    public Map<String, Object> toMap() {
        return new LinkedHashMap<>() {{
            put("labels", labels.asMap());
            put("state", state);
            put("started_at_epochms", started_epoch_ms);
            put("session_time_ms", session_time_ms);
            put("heartbeat_interval_ms", heartbeat_interval_ms);
            put("heartbeat_epoch_ms", heartbeat_epoch_ms);
            put("substatus", substatus);
        }};
    }

    @Override
    public String toString() {
        return dump.dumpToString(toMap());
    }
}
