/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.stateful.from_long;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.LongFunction;

/**
 * Load a named value from the per-thread state map.
 * The previous input value will be forgotten, and the named value will replace it
 * before the next function in the chain.
 */
@ThreadSafeMapper
@Categories({Category.state})
public class Load implements LongFunction<Object> {

    private final String name;
    private final LongFunction<Object> nameFunc;
    private final Object defaultValue;

    @Example({"Load('foo')","for the current thread, load an Object value from the named variable"})
    public Load(String name) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue=null;
    }

    @Example({"Load(NumberNameToString())","for the current thread, load an Object value from the named variable, where the variable name is returned by the provided function"})
    public Load(LongFunction<Object> nameFunc) {
        this.name = null;
        this.nameFunc = nameFunc;
        this.defaultValue=null;
    }

    @Example({"Load('foo','testvalue')","for the current thread, load an Object value from the named variable, " +
            "or the default value if the variable is not yet defined."})
    public Load(String name, Object defaultValue) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue=defaultValue;
    }

    @Example({"Load(NumberNameToString(),'testvalue')","for the current thread, load an Object value from the named " +
            "variable, where the variable name is returned by the provided function, or the" +
            "default value if the variable is not yet defined."})
    public Load(LongFunction<Object> nameFunc, Object defaultValue) {
        this.name = null;
        this.nameFunc = nameFunc;
        this.defaultValue = defaultValue;
    }

    @Override
    public Object apply(long value) {
        String varname = (nameFunc!=null) ? String.valueOf(nameFunc.apply(value)) : name;
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        Object output = map.getOrDefault(varname,defaultValue);
        return output;
    }
}
