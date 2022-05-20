package io.nosqlbench.virtdata.library.basics.shared.from_long.to_int;

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
import java.util.function.LongToIntFunction;

/**
 * Allow for the use of arbitrary expressions according to the
 * <a href="http://mvel.documentnode.com/">MVEL</a> expression language.
 *
 * Variables that have been set by a Save function are available
 * to be used in this function.
 *
 * The variable name <code>cycle</code> is reserved, and is always equal to
 * the current input value. This is not the same in every case as the
 * current cycle of an operation. It could be different if there
 * are preceding functions which modify the input value.
 */
@ThreadSafeMapper
@Categories({Category.functional})
public class Expr implements LongToIntFunction {

    private final String expr;
    private final Serializable compiledExpr;


    public Expr(String expr) {
        this.expr = expr;
        this.compiledExpr = MVELExpr.compile(long.class, "cycle", expr);
    }

    @Override
    public int applyAsInt(long value) {
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        map.put("cycle",value);
        int result = MVEL.executeExpression(compiledExpr, map, int.class);
        return result;
    }
}
