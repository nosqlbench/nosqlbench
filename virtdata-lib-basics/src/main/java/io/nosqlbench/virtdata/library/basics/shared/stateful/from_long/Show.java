package io.nosqlbench.virtdata.library.basics.shared.stateful.from_long;

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
import java.util.function.LongFunction;

@ThreadSafeMapper
@Categories({Category.state,Category.diagnostics})
public class Show implements LongFunction<String> {

    private final String[] names;
    private final ThreadLocal<StringBuilder> tl_sb = ThreadLocal.withInitial(StringBuilder::new);

    @Example({"Show()","Show all values in a json-like format"})
    public Show() {
        names=null;
    }

    @Example({"Show('foo')","Show only the 'foo' value in a json-like format"})
    @Example({"Show('foo','bar')","Show the 'foo' and 'bar' values in a json-like format"})
    public Show(String... names) {
        this.names = names;
    }

    @Override
    public String apply(long value) {
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        if (names==null) {
            return map.toString();
        }

        StringBuilder sb = tl_sb.get();
        sb.setLength(0);
        sb.append("{");

        for (String name : names) {
            sb.append(name).append("=");
            Object val = map.get(name);
            sb.append(val==null ? "NULL" : val.toString());
            sb.append(",");
        }
        sb.setLength(sb.length()-1);
        sb.append("}");

        return sb.toString();
    }
}
