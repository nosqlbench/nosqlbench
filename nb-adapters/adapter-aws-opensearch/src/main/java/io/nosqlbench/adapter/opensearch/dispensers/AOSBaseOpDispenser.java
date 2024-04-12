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

package io.nosqlbench.adapter.opensearch.dispensers;

import io.nosqlbench.adapter.opensearch.AOSAdapter;
import io.nosqlbench.adapter.opensearch.AOSSpace;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.opensearch.client.opensearch.OpenSearchClient;

import java.util.function.LongFunction;

public abstract class AOSBaseOpDispenser extends BaseOpDispenser<Op,Object> {
    protected final LongFunction<AOSSpace> spaceF;
    protected final LongFunction<OpenSearchClient> clientF;
    private final LongFunction<? extends Op> opF;

    protected AOSBaseOpDispenser(AOSAdapter adapter, ParsedOp op, LongFunction<String> targetF) {
        super(adapter, op);
        this.spaceF =adapter.getSpaceFunc(op);
        this.clientF = (long l) -> this.spaceF.apply(l).getClient();
        this.opF = createOpFunc(clientF, op, targetF);
    }

    public abstract LongFunction<? extends Op> createOpFunc(
        LongFunction<OpenSearchClient> clientF,
        ParsedOp op,
        LongFunction<String> targetF
    );

    @Override
    public Op getOp(long value) {
        return opF.apply(value);
    }
}
