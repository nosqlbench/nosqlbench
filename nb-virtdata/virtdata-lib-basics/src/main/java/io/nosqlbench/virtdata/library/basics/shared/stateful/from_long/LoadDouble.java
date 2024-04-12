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
import java.util.function.LongToDoubleFunction;

/**
 * Load a value from a named thread-local variable, where the variable
 * name is fixed or a generated variable name from a provided function.
 * If the named variable is not defined, then the default value is returned.
 */
@Categories(Category.state)
@ThreadSafeMapper
public class LoadDouble implements LongToDoubleFunction {

    private final String name;
    private final LongFunction<Object> nameFunc;
    private final double defaultValue;

    @Example({"LoadDouble('foo')","for the current thread, load a double value from the named variable."})
    public LoadDouble(String name) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue=0.0D;
    }

    @Example({"LoadDouble('foo',23D)","for the current thread, load a double value from the named variable," +
            "or the default value if the named variable is not defined."})
    public LoadDouble(String name, double defaultValue) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue=defaultValue;
    }

    @Example({"LoadDouble(NumberNameToString())","for the current thread, load a double value from the named variable, " +
            "where the variable name is provided by a function."})
    public LoadDouble(LongFunction<Object> nameFunc) {
        this.name=null;
        this.nameFunc = nameFunc;
        this.defaultValue=0.0D;
    }

    @Example({"LoadDouble(NumberNameToString(),23D)","for the current thread, load a double value from the named variable," +
            "where the variable name is provided by a function, or the default value if the named" +
            "variable is not defined."})
    public LoadDouble(LongFunction<Object> nameFunc, double defaultValue) {
        this.name=null;
        this.nameFunc = nameFunc;
        this.defaultValue=defaultValue;
    }

    @Override
    public double applyAsDouble(long value) {
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        String varname=(nameFunc!=null) ? String.valueOf(nameFunc.apply(value)) : name;
        Object loaded = map.getOrDefault(varname, defaultValue);
        return (Double) loaded;
    }
}
