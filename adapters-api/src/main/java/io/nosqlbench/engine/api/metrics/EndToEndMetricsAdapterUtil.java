package io.nosqlbench.engine.api.metrics;

import java.util.Map;
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

        MSG_SEQ_ERROR_SIMU_TYPE(String label) {
            this.label = label;
        }

        private static final Map<String, MSG_SEQ_ERROR_SIMU_TYPE> MAPPING = Stream.of(values())
            .flatMap(simuType ->
                Stream.of(simuType.label,
                        simuType.label.toLowerCase(),
                        simuType.label.toUpperCase(),
                        simuType.name(),
                        simuType.name().toLowerCase(),
                        simuType.name().toUpperCase())
                    .distinct().map(key -> Map.entry(key, simuType)))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        public static Optional<MSG_SEQ_ERROR_SIMU_TYPE> parseSimuType(String simuTypeString) {
            return Optional.ofNullable(MAPPING.get(simuTypeString.trim()));
        }
    }
}
