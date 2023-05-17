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
        this.veniceAdapterMetrics = new VeniceAdapterMetrics(this);
        veniceAdapterMetrics.initVeniceAdapterInstrumentation();
    }

    public VeniceSpace getVeniceSpace() { return veniceSpace; }
    public VeniceAdapterMetrics getVeniceAdapterMetrics() { return veniceAdapterMetrics; }

    // Mandatory Op parameter. Throw an error if not specified or having empty value
    protected LongFunction<String> lookupMandtoryStrOpValueFunc(String paramName) {
        LongFunction<String> stringLongFunction;
        stringLongFunction = parsedOp.getAsRequiredFunction(paramName, String.class);
        logger.info("{}: {}", paramName, stringLongFunction.apply(0));

        return stringLongFunction;
    }


}
