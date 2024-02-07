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

package io.nosqlbench.adapter.opensearch.ops;

import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import org.opensearch.client.opensearch.OpenSearchClient;

public abstract class BaseOpenSearchOp implements CycleOp<Object> {
    protected final OpenSearchClient client;

    public BaseOpenSearchOp(OpenSearchClient client) {
        this.client = client;
    }

    @Override
    public abstract Object apply(long value);
}
