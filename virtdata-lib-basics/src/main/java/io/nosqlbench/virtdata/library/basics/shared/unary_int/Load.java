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

package io.nosqlbench.virtdata.library.basics.shared.unary_int;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

@Categories(Category.state)
@ThreadSafeMapper
public class Load implements IntUnaryOperator {

    private final String name;
    private final Function<Object,Object> nameFunc;
    private final int defaultValue;


    @Example({"Load('foo')","for the current thread, load an int value from the named variable"})
    public Load(String name) {
        this.name = name;
        this.nameFunc =null;
        this.defaultValue=0;
    }

    @Example({"Load('foo',42)","for the current thread, load an int value from the named variable, or return the default value if it is undefined."})
    public Load(String name, int defaultValue) {
        this.name = name;
        this.nameFunc =null;
        this.defaultValue=defaultValue;
    }

    @Example({"Load(NumberNameToString())","for the current thread, load an int value from the named variable, where the variable name is provided by a function."})
    public Load(Function<Object,Object> nameFunc) {
        this.name = null;
        this.nameFunc =nameFunc;
        this.defaultValue=0;
    }

    @Example({"Load(NumberNameToString(),42)","for the current thread, load an int value from the named variable, where the variable name is provided by a function, or the default value if the named variable is undefined."})
    public Load(Function<Object,Object> nameFunc, int defaultValue) {
        this.name = null;
        this.nameFunc =nameFunc;
        this.defaultValue=defaultValue;
    }


    @Override
    public int applyAsInt(int operand) {
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        String varname = nameFunc !=null ? String.valueOf(nameFunc.apply(operand)) : name;
        Object o = map.getOrDefault(varname,defaultValue);
        return (int) o;
    }

}
