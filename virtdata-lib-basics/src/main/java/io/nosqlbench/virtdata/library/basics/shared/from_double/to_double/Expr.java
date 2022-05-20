package io.nosqlbench.virtdata.library.basics.shared.from_double.to_double;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.MVELExpr;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;
import org.mvel2.MVEL;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleUnaryOperator;

@ThreadSafeMapper
@Categories({Category.functional})
public class Expr implements DoubleUnaryOperator {

    private final String expr;
    private final Serializable compiledExpr;

    public Expr(String expr) {
        this.expr = expr;
        this.compiledExpr = MVELExpr.compile(double.class, "cycle", expr);
    }

    @Override
    public double applyAsDouble(double operand) {
        ConcurrentHashMap<String, Object> gl_map = SharedState.gl_ObjectMap;
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();

        // merge gl into tl, for duplicates use the value from tl
        for (Map.Entry<String, Object> stringObjectEntry : gl_map.entrySet()) {
            map.merge(stringObjectEntry.getKey(), stringObjectEntry.getValue(), (entry1, entry2) -> entry1);
        }

        map.put("cycle",operand);
        double result = MVEL.executeExpression(compiledExpr, map, double.class);
        return result;
    }
}
