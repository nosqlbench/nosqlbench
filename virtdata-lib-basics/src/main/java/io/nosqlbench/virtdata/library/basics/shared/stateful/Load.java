package io.nosqlbench.virtdata.library.basics.shared.stateful;

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
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.state})
public class Load implements Function<Object,Object> {

    private final String name;
    private final Function<Object,Object> nameFunc;
    private final Object defaultValue;

    @Example({"Load('foo')","for the current thread, load an Object value from the named variable"})
    public Load(String name) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue=null;
    }

    @Example({"Load(NumberNameToString())","for the current thread, load an Object value from the named variable, where the variable name is returned by the provided function"})
    public Load(Function<Object,Object> nameFunc) {
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
    public Load(Function<Object,Object> nameFunc, Object defaultValue) {
        this.name = null;
        this.nameFunc = nameFunc;
        this.defaultValue = defaultValue;
    }

    @Override
    public Object apply(Object o) {
        String varname = (nameFunc!=null) ? String.valueOf(nameFunc.apply(o)) : name;
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        Object output = map.getOrDefault(varname,defaultValue);
        return output;
    }

}
