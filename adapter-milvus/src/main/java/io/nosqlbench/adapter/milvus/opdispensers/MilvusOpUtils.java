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

package io.nosqlbench.adapter.milvus.opdispensers;

import com.alibaba.fastjson.JSONObject;
import io.milvus.param.dml.InsertParam;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

public class MilvusOpUtils {

    public static Optional<LongFunction<List<JSONObject>>> getHighLevelRowsFunction(ParsedOp op, String opfield) {
        if (!op.isDefined(opfield)) {
            return Optional.empty();
        }

        LongFunction<Object> rowF = op.getAsRequiredFunction(opfield, Object.class);
        Object testObject = rowF.apply(0L);
        LongFunction<List<JSONObject>> rowsF = null;
        if (testObject instanceof JSONObject) {
            rowsF = l -> List.of((JSONObject) rowF.apply(l));
        } else if (testObject instanceof Map) {
            rowsF = l -> List.of(new JSONObject((Map<String, Object>) rowF.apply(l)));
        } else if (testObject instanceof List list) {
            if (list.size() == 0) {
                throw new OpConfigError("Unable to detect type of list object for empty list for op named '" + op.getName() + "'");
            } else if (list.get(0) instanceof JSONObject) {
                rowsF = l -> (List<JSONObject>) rowF.apply(l);
            } else if (list.get(0) instanceof Map) {
                rowsF = l -> List.of(new JSONObject((Map<String, Object>) rowF.apply(l)));
            }
        }
        return Optional.ofNullable(rowsF);
//
//        if (rowsF==null) {
//            throw new OpConfigError("A Milvus row list can only be created from " +
//                "1) a JSONObject (1 row), " +
//                "2) A Map (1 row), " +
//                "3) a List of JSONObject (multiple rows), or " +
//                "4) a list of Maps (multiple rows)");
//        }

    }

    public static Optional<LongFunction<List<InsertParam.Field>>> getFieldsFunction(ParsedOp op, String opfield) {
        if (!op.isDefined(opfield)) {
            return Optional.empty();
        }
        ParsedOp valueTemplate = op.getAsSubOp(opfield, ParsedOp.SubOpNaming.SubKey);
        Map<String, Object> testFieldsValues = valueTemplate.apply(0L);

        List<LongFunction<InsertParam.Field>> fieldsF = new ArrayList<>(testFieldsValues.size());

        for (String fieldName : testFieldsValues.keySet()) {
            Object testFieldValue = testFieldsValues.get(fieldName);
            if (!(testFieldValue instanceof List<?> list)) {
                throw new OpConfigError("Every value provided to a named field must be a List, not " + testFieldValue.getClass().getSimpleName());
            }
        }
        LongFunction<List<InsertParam.Field>> f = new FieldsFuncFromMap(valueTemplate::apply);
        return Optional.of(f);
    }

    private record FieldsFuncFromMap(
        LongFunction<Map<String, Object>> generator
    ) implements LongFunction<List<InsertParam.Field>> {

        @Override
        public List<InsertParam.Field> apply(long value) {
            Map<String, Object> datamap = generator.apply(value);
            ArrayList<InsertParam.Field> fields = new ArrayList<>(datamap.size());
            datamap.forEach((k,v) -> {
                fields.add(new InsertParam.Field(k,(List)v));
            });
            return fields;
        }
    }

}
