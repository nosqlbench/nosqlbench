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

package io.nosqlbench.engine.extensions.vectormath;

import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.pinecone.proto.QueryResponse;
import io.pinecone.proto.ScoredVector;

public class PineconeScriptingUtils extends NBBaseComponent {

    public PineconeScriptingUtils(NBComponent parentComponent) {
        super(parentComponent);
    }

    public String[] responseIdsToStringArray(QueryResponse response) {
        return response.getMatchesList().stream().map(ScoredVector::getId).toArray(String[]::new);
    }

    public int[] responseIdsToIntArray(QueryResponse response) {
        return response.getMatchesList().stream().mapToInt(r -> Integer.parseInt(r.getId())).toArray();
    }

    public long[] responseIdsToLongArray(QueryResponse response) {
        return response.getMatchesList().stream().mapToLong(r -> Long.parseLong(r.getId())).toArray();
    }

    public String[] responseFieldToStringArray(String fieldname, QueryResponse response) {
        return response.getMatchesList().stream().map(r -> r.getMetadata().getFieldsMap()
            .get(fieldname).getStringValue()).toArray(String[]::new);
    }

    public int[] responseFieldToIntArray(String fieldname, QueryResponse response) {
        return response.getMatchesList().stream().mapToInt(r -> Integer.parseInt(r.getMetadata()
            .getFieldsMap().get(fieldname).getStringValue())).toArray();
    }

    public long[] responseFieldToLongArray(String fieldname, QueryResponse response) {
        return response.getMatchesList().stream().mapToLong(r -> Long.parseLong(r.getMetadata()
            .getFieldsMap().get(fieldname).getStringValue())).toArray();
    }

}
