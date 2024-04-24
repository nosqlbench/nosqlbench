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
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
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

    protected LongFunction<List<Float>> extractFloatVals(LongFunction<Object> af) {
        return l -> this.getVectorValues(af.apply(l));
    }

    protected Map<String, Value> generateMetadataMap(Map<String, Object> metadata_values_map) {
        Map<String, Value> metadata_map = new HashMap<>();
        BiConsumer<String,Object> stringToValue = (key, val) -> {
            Value targetval;
            if (val instanceof String) targetval = Value.newBuilder().setStringValue((String)val).build();
            else if (val instanceof Number) targetval = Value.newBuilder().setNumberValue((((Number) val).doubleValue())).build();
            else if (val instanceof List) targetval = Value.newBuilder().setListValue(generateListValue((List) val)).build();
            else if (val instanceof Boolean) targetval = Value.newBuilder().setBoolValue((Boolean) val).build();
            else throw new RuntimeException("Unsupported metadata value type");
            metadata_map.put(key, targetval);
        };
        metadata_values_map.forEach(stringToValue);
        return metadata_map;
    }

    protected List<Float> getVectorValues(Object rawVectorValues) {
        List<Float> floatValues;
        if (rawVectorValues instanceof String) {
            floatValues = new ArrayList<>();
            String[] rawValues = (((String) rawVectorValues).split(","));
            for (String val : rawValues) {
                floatValues.add(Float.valueOf(val));
            }
        } else if (rawVectorValues instanceof List) {
            floatValues = switch (((List<?>) rawVectorValues).get(0).getClass().getSimpleName()) {
                case "Float" -> (List<Float>) rawVectorValues;
                case "Double" -> ((List<Double>) rawVectorValues).stream().map(Double::floatValue).toList();
                case "String" -> ((List<String>) rawVectorValues).stream().map(Float::parseFloat).toList();
                default -> throw new RuntimeException("Invalid type specified for values");
            };
        } else {
            throw new RuntimeException("Invalid type specified for values");
        }
        return floatValues;
    }

    protected List<Integer> getIndexValues(Object rawIndexValues) {
        List<Integer> intValues;
        if (rawIndexValues instanceof String) {
            intValues = new ArrayList<>();
            String[] rawValues = (((String) rawIndexValues).split(","));
            for (String val : rawValues) {
                intValues.add(Integer.valueOf(val));
            }
        } else if (rawIndexValues instanceof List) {
            intValues = (List<Integer>) rawIndexValues;
        }else {
            throw new RuntimeException("Invalid type specified for Index values");
        }
        return intValues;
    }

}
