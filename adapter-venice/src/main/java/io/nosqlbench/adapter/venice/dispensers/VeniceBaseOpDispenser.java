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

package io.nosqlbench.adapter.venice.dispensers;

import io.nosqlbench.adapter.venice.VeniceSpace;
import io.nosqlbench.adapter.venice.ops.VeniceOp;
import io.nosqlbench.adapter.venice.util.*;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract  class VeniceBaseOpDispenser extends BaseOpDispenser<VeniceOp, VeniceSpace> {

    private static final Logger logger = LogManager.getLogger("VeniceBaseOpDispenser");

    protected final ParsedOp parsedOp;
    protected final VeniceSpace veniceSpace;
    protected final VeniceAdapterMetrics veniceAdapterMetrics;

    protected VeniceBaseOpDispenser(DriverAdapter adapter,
                                 ParsedOp op,
                                 VeniceSpace veniceSpace) {

        super(adapter, op);

        this.parsedOp = op;
        this.veniceSpace = veniceSpace;
        String defaultMetricsPrefix = parsedOp.getLabels().linearize("activity");
        this.veniceAdapterMetrics = new VeniceAdapterMetrics(defaultMetricsPrefix);
        veniceAdapterMetrics.initVeniceAdapterInstrumentation();
    }

    public VeniceSpace getVeniceSpace() { return veniceSpace; }
    public VeniceAdapterMetrics getVeniceAdapterMetrics() { return veniceAdapterMetrics; }

    protected LongFunction<Boolean> lookupStaticBoolConfigValueFunc(String paramName, boolean defaultValue) {
        LongFunction<Boolean> booleanLongFunction;
        booleanLongFunction = l -> parsedOp.getOptionalStaticConfig(paramName, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(value -> BooleanUtils.toBoolean(value))
            .orElse(defaultValue);
        logger.info("{}: {}", paramName, booleanLongFunction.apply(0));
        return  booleanLongFunction;
    }

    protected LongFunction<Set<String>> lookupStaticStrSetOpValueFunc(String paramName) {
        LongFunction<Set<String>> setStringLongFunction;
        setStringLongFunction = l -> parsedOp.getOptionalStaticValue(paramName, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(value -> {
                Set<String > set = new HashSet<>();

                if (StringUtils.contains(value,',')) {
                    set = Arrays.stream(value.split(","))
                        .map(String::trim)
                        .filter(Predicate.not(String::isEmpty))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                }

                return set;
            }).orElse(Collections.emptySet());
        logger.info("{}: {}", paramName, setStringLongFunction.apply(0));
        return setStringLongFunction;
    }

    // If the corresponding Op parameter is not provided, use the specified default value
    protected LongFunction<Integer> lookupStaticIntOpValueFunc(String paramName, int defaultValue) {
        LongFunction<Integer> integerLongFunction;
        integerLongFunction = l -> parsedOp.getOptionalStaticValue(paramName, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(value -> NumberUtils.toInt(value))
            .map(value -> {
                if (0 > value) return 0;
                return value;
            }).orElse(defaultValue);
        logger.info("{}: {}", paramName, integerLongFunction.apply(0));
        return integerLongFunction;
    }

    // If the corresponding Op parameter is not provided, use the specified default value
    protected LongFunction<String> lookupOptionalStrOpValueFunc(String paramName, String defaultValue) {
        LongFunction<String> stringLongFunction;
        stringLongFunction = parsedOp.getAsOptionalFunction(paramName, String.class)
            .orElse(l -> defaultValue);
        logger.info("{}: {}", paramName, stringLongFunction.apply(0));

        return stringLongFunction;
    }
    protected LongFunction<String> lookupOptionalStrOpValueFunc(String paramName) {
        return lookupOptionalStrOpValueFunc(paramName, "");
    }

    // Mandatory Op parameter. Throw an error if not specified or having empty value
    protected LongFunction<String> lookupMandtoryStrOpValueFunc(String paramName) {
        LongFunction<String> stringLongFunction;
        stringLongFunction = parsedOp.getAsRequiredFunction(paramName, String.class);
        logger.info("{}: {}", paramName, stringLongFunction.apply(0));

        return stringLongFunction;
    }


}
