package io.nosqlbench.virtdata.library.basics.shared.functionadapters;

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
import io.nosqlbench.virtdata.core.composers.FunctionAssembly;
import io.nosqlbench.virtdata.core.composers.FunctionComposer;
import io.nosqlbench.virtdata.core.bindings.ResolvedFunction;
import io.nosqlbench.virtdata.api.bindings.VirtDataFunctions;

import java.util.function.LongFunction;

/**
 * <p>Combine functions into one.</p>
 *
 * <p>This function allows you to combine multiple other functions into one. This is often useful
 * for constructing more sophisticated recipes, when you don't have the ability to use
 * control flow or non-functional forms.</p>
 *
 * <p>The functions will be stitched together using the same logic that VirtData uses when
 * combining flows outside functions. That said, if the functions selected are not the right ones,
 * then it is possible to end up with the wrong data type at the end. To remedy this, be sure
 * to add input and output qualifiers, like <code>long-&gt;</code> or <code>-&gt;String</code> where
 * appropriate, to ensure that VirtData selects the right functions within the flow.</p>
 */
@Categories(Category.conversion)
@ThreadSafeMapper
public class Flow implements LongFunction<Object> {

    private final LongFunction f;

    public Flow(Object... funcs) {
        FunctionComposer assembly = new FunctionAssembly();
        for (Object func : funcs) {
            assembly = assembly.andThen(func);
        }
        ResolvedFunction rf = assembly.getResolvedFunction();
        Object functionObject = rf.getFunctionObject();
        f = VirtDataFunctions.adapt(functionObject,LongFunction.class, Object.class, true);
//        f = LongFunction.class.cast(functionObject);
    }

    @Override
    public Object apply(long value) {
        Object o = f.apply(value);
        return o;
    }
}
