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
 *
 */

package io.nosqlbench.adapter.gcpspanner.opdispensers;

import io.nosqlbench.adapter.gcpspanner.GCPSpannerDriverAdapter;
import io.nosqlbench.adapter.gcpspanner.ops.GCPSpannerBaseOp;
import io.nosqlbench.adapter.gcpspanner.ops.GCPSpannerUpdateDatabaseDdlOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class GCPSpannerUpdateDatabaseDdlOpDispenser extends GCPSpannerBaseOpDispenser {
    private static final Logger logger = LogManager.getLogger(GCPSpannerUpdateDatabaseDdlOpDispenser.class);
    private final LongFunction<GCPSpannerUpdateDatabaseDdlOp> opFunction;

    public GCPSpannerUpdateDatabaseDdlOpDispenser(GCPSpannerDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = createOpFunction(op);
    }

    private LongFunction<GCPSpannerUpdateDatabaseDdlOp> createOpFunction(ParsedOp op) {

        return (l) -> new GCPSpannerUpdateDatabaseDdlOp(
            spaceFunction.apply(l).getSpanner(),
            l,
            targetFunction.apply(l),
            spaceFunction.apply(l).getDbAdminClient(),
            spaceFunction.apply(l).getDbAdminClient().getDatabase(spaceFunction.apply(l).getInstanceId(), spaceFunction.apply(l).getDatabaseIdString())
        );
    }

    @Override
    public GCPSpannerBaseOp<?> getOp(long value) {
        return opFunction.apply(value);
    }
}
