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

package io.nosqlbench.virtdata.library.basics.shared.stateful;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.Function;

/**
 * Save a value to a named thread-local variable, where the variable
 * name is fixed or a generated variable name from a provided function.
 * Note that the input type is not that suitable for constructing names,
 * so this is more likely to be used in an indirect naming pattern like
 * SaveString(Load('id'))
 */
@Categories(Category.state)
@ThreadSafeMapper
public class SaveString implements Function<String,String> {

    private final String name;
    private final Function<Object,Object> nameFunc;

    @Example({"SaveString('foo')","save the current String value to a named variable in this thread."})
    public SaveString(String name) {
        this.name = name;
        this.nameFunc=null;
    }

    @Example({"SaveString(NumberNameToString())","save the current String value to a named variable in this thread" +
            ", where the variable name is provided by a function."})
    public SaveString(Function<Object,Object> nameFunc) {
        this.name=null;
        this.nameFunc = nameFunc;
    }

    @Override
    public String apply(String s) {
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        String varname=(nameFunc!=null) ? String.valueOf(nameFunc.apply(s)) : name;
        map.put(varname,s);
        return s;
    }
}
