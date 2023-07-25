/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.adapters.api.metrics;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EndToEndMetricsAdapterUtil {
    ///////
    // Message processing sequence error simulation types
    public enum MSG_SEQ_ERROR_SIMU_TYPE {
        OutOfOrder("out_of_order"),
        MsgLoss("msg_loss"),
        MsgDup("msg_dup");

        public final String label;

        MSG_SEQ_ERROR_SIMU_TYPE(final String label) {
            this.label = label;
        }

        private static final Map<String, MSG_SEQ_ERROR_SIMU_TYPE> MAPPING = Stream.of(MSG_SEQ_ERROR_SIMU_TYPE.values())
            .flatMap(simuType ->
                Stream.of(simuType.label,
                        simuType.label.toLowerCase(),
                        simuType.label.toUpperCase(),
                        simuType.name(),
                        simuType.name().toLowerCase(),
                        simuType.name().toUpperCase())
                    .distinct().map(key -> Map.entry(key, simuType)))
            .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));

        public static Optional<MSG_SEQ_ERROR_SIMU_TYPE> parseSimuType(final String simuTypeString) {
            return Optional.ofNullable(MSG_SEQ_ERROR_SIMU_TYPE.MAPPING.get(simuTypeString.trim()));
        }
    }
}
