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

package io.nosqlbench.adapter.pinecone.opdispensers;

import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.nosqlbench.adapter.pinecone.PineconeDriverAdapter;
import io.nosqlbench.adapter.pinecone.PineconeSpace;
import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

public abstract class PineconeOpDispenser extends BaseOpDispenser<PineconeOp, PineconeSpace> {
    protected final LongFunction<PineconeSpace> pcFunction;
    protected final LongFunction<String> targetFunction;

    protected PineconeOpDispenser(PineconeDriverAdapter adapter,
                                  ParsedOp op,
                                  LongFunction<PineconeSpace> pcFunction,
                                  LongFunction<String> targetFunction) {
        super(adapter, op);
        this.pcFunction = pcFunction;
        this.targetFunction = targetFunction;
    }

    protected LongFunction<Struct> buildFilterStruct(LongFunction<Map> filterFunction) {
        return  l -> {
            Map<String,Object> filterFields = filterFunction.apply(l);
            Value comparatorVal;
            Object comparator = filterFields.get("comparator");
            if (comparator instanceof String) {
                comparatorVal = Value.newBuilder().setStringValue((String) comparator).build();
            } else if (comparator instanceof Number) {
                comparatorVal = Value.newBuilder().setNumberValue((Double) comparator).build();
            } else if (comparator instanceof List) {
                comparatorVal = Value.newBuilder().setListValue(generateListValue((List) comparator)).build();
            } else {
                throw new RuntimeException("Invalid type for filter comparator specified");
            }
            return Struct.newBuilder().putFields((String) filterFields.get("filterfield"),
                Value.newBuilder().setStructValue(
                        Struct.newBuilder().putFields((String) filterFields.get("operator"),
                            comparatorVal))
                    .build()).build();
        };
    }

    protected ListValue generateListValue(List comparator) {
        ListValue.Builder listValueBuilder = ListValue.newBuilder();
        for (Object entry : comparator) {
            Value value = Value.newBuilder().setStringValue(String.valueOf(entry)).build();
            listValueBuilder.addValues(value);
        }
        return listValueBuilder.build();
    }



}
