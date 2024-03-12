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

public abstract class AOSBaseOp implements CycleOp<Object> {
    protected final OpenSearchClient client;

    public AOSBaseOp(OpenSearchClient client) {
        this.client = client;
    }

    @Override
    public final Object apply(long value) {
        try {
            Object result = applyOp(value);
            return result;
        } catch (Exception e) {
            if (e instanceof RuntimeException rte) {
                throw rte;
            } else {
                throw new RuntimeException(e);
            }
        }
    };

    public abstract Object applyOp(long value) throws Exception;
}
