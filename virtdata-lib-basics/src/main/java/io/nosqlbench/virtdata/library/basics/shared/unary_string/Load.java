package io.nosqlbench.virtdata.library.basics.shared.unary_string;

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

@Categories(Category.state)
@ThreadSafeMapper
public class Load implements Function<String,String> {

    private final String name;
    private final Function<Object,Object> nameFunc;
    private final String defaultValue;


    @Example({"Load('foo')","for the current thread, load a String value from the named variable"})
    public Load(String name) {
        this.name = name;
        this.nameFunc = null;
        this.defaultValue=null;
    }

    @Example({"Load('foo','track05')","for the current thread, load a String value from the named variable, or teh default value if the variable is not yet defined."})
    public Load(String name, String defaultvalue) {
        this.name = name;
        this.nameFunc = null;
        this.defaultValue=defaultvalue;
    }

    @Example({"Load(NumberNameToString())","for the current thread, load a String value from the named variable, where the variable name is provided by a function"})
    public Load(Function<Object,Object> nameFunc) {
        this.name = null;
        this.nameFunc = nameFunc;
        this.defaultValue=null;
    }

    @Example({"Load(NumberNameToString(),'track05')","for the current thread, load a String value from the named variable, where the variable name is provided by a function, or the default value if the variable is not yet defined."})
    public Load(Function<Object,Object> nameFunc, String defaultValue) {
        this.name = null;
        this.nameFunc = nameFunc;
        this.defaultValue=defaultValue;
    }

    @Override
    public String apply(String s) {
        String varname = nameFunc !=null ? String.valueOf(nameFunc.apply(s)) : name;
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        Object output = map.getOrDefault(varname,defaultValue);
        return (String) output;
    }

}
