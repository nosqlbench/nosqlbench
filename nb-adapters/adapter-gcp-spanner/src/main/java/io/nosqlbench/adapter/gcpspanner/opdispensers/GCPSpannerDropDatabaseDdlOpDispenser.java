/*
 * Copyright (c) 2020-2024 nosqlbench
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

package io.nosqlbench.adapter.gcpspanner.opdispensers;


import io.nosqlbench.adapter.gcpspanner.GCPSpannerDriverAdapter;
import io.nosqlbench.adapter.gcpspanner.ops.GCPSpannerBaseOp;
import io.nosqlbench.adapter.gcpspanner.ops.GCPSpannerDropDatabaseDdlOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

/**
 * Dispenser class for creating databases of {@link GCPSpannerDropDatabaseDdlOp}.
 *
 * @see <a href="https://cloud.google.com/spanner/docs/reference/rpc/google.spanner.admin.database.v1#dropdatabaserequest">
 *     DropDatabaseRequest</a> which can be a stretch goal to combine all of DB, Table(s), and Indexes into one-single call.
 */
public class GCPSpannerDropDatabaseDdlOpDispenser extends GCPSpannerBaseOpDispenser {
    private static final Logger logger = LogManager.getLogger(GCPSpannerDropDatabaseDdlOpDispenser.class);
    private final LongFunction<GCPSpannerDropDatabaseDdlOp> opFunction;

    /**
     * Constructor for {@link GCPSpannerDropDatabaseDdlOpDispenser}.
     *
     * @param adapter        the {@link GCPSpannerDriverAdapter} instance
     * @param op             the {@link ParsedOp} instance
     * @param targetFunction a {@link LongFunction} that provides the target string
     */
    public GCPSpannerDropDatabaseDdlOpDispenser(GCPSpannerDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
        this.opFunction = DropOpFunction(op);
    }

    /**
     * Drops a {@link LongFunction} that generates {@link GCPSpannerDropDatabaseDdlOp} instances.
     *
     * @param op the {@link ParsedOp} instance
     * @return a {@link LongFunction} that generates {@link GCPSpannerDropDatabaseDdlOp} instances
     */
    private LongFunction<GCPSpannerDropDatabaseDdlOp> DropOpFunction(ParsedOp op) {
        return (l) -> new GCPSpannerDropDatabaseDdlOp(
            spaceFunction.apply(l).getSpanner(),
            l,
            targetFunction.apply(l),
            spaceFunction.apply(l).getDbAdminClient(),
            spaceFunction.apply(l).getDbAdminClient().getDatabase(spaceFunction.apply(l).getInstanceId(), spaceFunction.apply(l).getDatabaseIdString())
        );
    }

    /**
     * Retrieves an operation instance based on the provided value.
     *
     * @param value the long value used to generate the operation
     * @return a {@link GCPSpannerBaseOp} instance
     */
    @Override
    public GCPSpannerBaseOp<?> getOp(long value) {
        return opFunction.apply(value);
    }
}
