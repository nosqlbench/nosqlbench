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
 *
 */

package io.nosqlbench.engine.extensions.vectormath;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.nosqlbench.nb.api.components.NBBaseComponent;
import io.pinecone.proto.QueryResponse;
import io.pinecone.proto.ScoredVector;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class PineconeScriptingUtilsTest {


    private QueryResponse generateQueryResponse() {
        Value four = Value.newBuilder().setStringValue("4").build();
        Value five = Value.newBuilder().setStringValue("5").build();
        Value six = Value.newBuilder().setStringValue("6").build();
        Map<String,Value> metamap = new HashMap<>();
        metamap.put("a",four);
        metamap.put("b",five);
        metamap.put("c",six);
        Struct metadata = Struct.newBuilder().putAllFields(metamap).build();
        return QueryResponse.newBuilder().addMatches(
            ScoredVector.newBuilder().setId("1").setMetadata(metadata).build()
        ).addMatches(
            ScoredVector.newBuilder().setId("2").setMetadata(metadata).build()
        ).addMatches(
            ScoredVector.newBuilder().setId("3").setMetadata(metadata).build()
        ).build();
    }

    @Test
    void responseIdsToStringArrayTest() {
        QueryResponse response = generateQueryResponse();
        PineconeScriptingUtils utils = new PineconeScriptingUtils(new NBBaseComponent(null));
        String[] ids = utils.responseIdsToStringArray(response);
        assert(ids.length == 3);
        assert(ids[0].equals("1"));
        assert(ids[1].equals("2"));
        assert(ids[2].equals("3"));
    }

    @Test
    void responseIdsToIntArrayTest() {
        QueryResponse response = generateQueryResponse();
        PineconeScriptingUtils utils = new PineconeScriptingUtils(new NBBaseComponent(null));
        int[] ids = utils.responseIdsToIntArray(response);
        assert(ids.length == 3);
        assert(ids[0] == 1);
        assert(ids[1] == 2);
        assert(ids[2] == 3);
    }

    @Test
    void responseIdsToLongArrayTest() {
        QueryResponse response = generateQueryResponse();
        PineconeScriptingUtils utils = new PineconeScriptingUtils(new NBBaseComponent(null));
        long[] ids = utils.responseIdsToLongArray(response);
        assert(ids.length == 3);
        assert(ids[0] == 1L);
        assert(ids[1] == 2L);
        assert(ids[2] == 3L);
    }

    @Test
    void responseFieldToStringArrayTest() {
        QueryResponse response = generateQueryResponse();
        PineconeScriptingUtils utils = new PineconeScriptingUtils(new NBBaseComponent(null));
        String[] ids = utils.responseFieldToStringArray("a", response);
        assert(ids.length == 3);
        assert(ids[0].equals("4"));
        assert(ids[1].equals("4"));
        assert(ids[2].equals("4"));
    }

    @Test
    void responseFieldToIntArrayTest() {
        QueryResponse response = generateQueryResponse();
        PineconeScriptingUtils utils = new PineconeScriptingUtils(new NBBaseComponent(null));
        int[] ids = utils.responseFieldToIntArray("b", response);
        assert(ids.length == 3);
        assert(ids[0] == 5);
        assert(ids[1] == 5);
        assert(ids[2] == 5);
    }

    @Test
    void responseFieldToLongArrayTest() {
        QueryResponse response = generateQueryResponse();
        PineconeScriptingUtils utils = new PineconeScriptingUtils(new NBBaseComponent(null));
        long[] ids = utils.responseFieldToLongArray("c", response);
        assert(ids.length == 3);
        assert(ids[0] == 6L);
        assert(ids[1] == 6L);
        assert(ids[2] == 6L);
    }




}
