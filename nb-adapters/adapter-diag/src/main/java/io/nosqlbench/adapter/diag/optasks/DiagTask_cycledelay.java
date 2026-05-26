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

package io.nosqlbench.adapter.diag.optasks;

import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;
import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;

import java.util.Map;
import java.util.concurrent.locks.LockSupport;
import java.util.function.LongUnaryOperator;
// LongUnaryOperator: long -> long; used to map a cycle index to a synthetic delay amount.

/// Per-cycle synthetic latency task for [`driver=diag`]. Sleeps the calling thread for a
/// binding-derived amount of time on every cycle, so synthetic workloads can produce realistic
/// per-op latency distributions without an external target system.
///
/// **Parameters**:
/// - `delay` (required): a [DataMapper] binding recipe that maps the cycle value to a numeric
///   delay amount, e.g. `HashedRange(500L, 2000L)` or `Normal(1500.0, 250.0)`.
/// - `unit` (optional, default `us`): the time unit for the delay amount. Accepted values:
///   `ns`, `us`, `ms`.
///
/// **Why a custom task**: existing diag tasks (`noop`, `initdelay`, `gauge`, `erroroncycle`,
/// `log`, `diagrate`) have no concept of per-cycle synthetic latency. `initdelay` is a one-time
/// setup pause, not a per-op delay; `gauge` produces gauge values but does not affect cycle
/// timing. Composed with multiple op blocks, `cycledelay` lets a stand-alone demo workload
/// emit per-op latency distributions that are interesting to MQL queries and the
/// `stress-report` app.
///
/// **Timing accuracy**: uses [LockSupport#parkNanos] in a deadline loop so spurious wakeups
/// don't return early. Accuracy is "OS scheduler good enough for demos" — not microsecond-
/// precise for sub-millisecond targets on contended systems.
@Service(value = DiagTask.class, selector = "cycledelay")
public class DiagTask_cycledelay extends BaseDiagTask {

    private LongUnaryOperator delayFunc;
    private long unitToNanos;

    @Override
    public Map<String, Object> apply(Long cycle, Map<String, Object> state) {
        long amount = delayFunc.applyAsLong(cycle);
        if (amount <= 0L) {
            return state;
        }
        long delayNs = amount * unitToNanos;
        long deadline = System.nanoTime() + delayNs;
        long remaining;
        while ((remaining = deadline - System.nanoTime()) > 0L) {
            LockSupport.parkNanos(remaining);
        }
        return state;
    }

    @Override
    public void applyConfig(NBConfiguration cfg) {
        String binding = cfg.get("delay", String.class);
        String unit = cfg.getOptional("unit").orElse("us");
        this.unitToNanos = switch (unit) {
            case "ns" -> 1L;
            case "us" -> 1_000L;
            case "ms" -> 1_000_000L;
            default -> throw new IllegalArgumentException(
                "Unknown 'unit' for cycledelay: '" + unit + "' (expected one of ns, us, ms)");
        };
        DataMapper<Object> mapper = VirtData.getMapper(binding, Map.of());
        this.delayFunc = VirtDataConversions.adaptFunction(mapper, LongUnaryOperator.class);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(DiagTask_cycledelay.class)
            .add(Param.required("name", String.class))
            .add(Param.required("delay", String.class))
            .add(Param.defaultTo("unit", "us"))
            .asReadOnly();
    }
}
