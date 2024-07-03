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

package io.nosqlbench.adapter.ragstack.opdispensers;

import io.nosqlbench.adapter.ragstack.RagstackDriverAdapter;
import io.nosqlbench.adapter.ragstack.ops.RagstackBaseOp;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class RagstackLoadDatasetOpDispenser extends RagstackOpDispenser {
    public RagstackLoadDatasetOpDispenser(RagstackDriverAdapter adapter, ParsedOp op, LongFunction<String> targetFunction) {
        super(adapter, op, targetFunction);
    }

    @Override
    public RagstackBaseOp getOp(long value) {
        return null;
    }
}
