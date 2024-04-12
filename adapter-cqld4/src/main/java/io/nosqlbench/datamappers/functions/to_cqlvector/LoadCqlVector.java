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

package io.nosqlbench.datamappers.functions.to_cqlvector;

import com.datastax.oss.driver.api.core.data.CqlVector;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.Function;

@Categories(Category.state)
@ThreadSafeMapper
public class LoadCqlVector implements Function<Object,com.datastax.oss.driver.api.core.data.CqlVector> {

    private final String name;
    private final Function<Object,Object> nameFunc;
    private final com.datastax.oss.driver.api.core.data.CqlVector defaultValue;

    @Example({"LoadDouble('foo')","for the current thread, load a double value from the named variable."})
    public LoadCqlVector(String name) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue=com.datastax.oss.driver.api.core.data.CqlVector.newInstance(0.0f);
    }

    @Example({"LoadDouble('foo',23D)","for the current thread, load a double value from the named variable," +
            "or the default value if the named variable is not defined."})
    public LoadCqlVector(String name, int len) {
        this.name = name;
        this.nameFunc=null;
        Double[] ary = new Double[len];
        for (int i = 0; i < len; i++) {
            ary[i]=(double)i;
        }
        this.defaultValue=com.datastax.oss.driver.api.core.data.CqlVector.newInstance(ary);
    }

    @Override
    public com.datastax.oss.driver.api.core.data.CqlVector apply(Object o) {
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        String varname=(nameFunc!=null) ? String.valueOf(nameFunc.apply(o)) : name;
        Object value = map.getOrDefault(varname, defaultValue);
        if (value instanceof CqlVector<?> cqlvector) {
            return cqlvector;
        } else if (value instanceof float[] fa) {
            Float[] ary = new Float[fa.length];
            for (int i = 0; i < fa.length; i++) {
                ary[i]=fa[i];
            }
            return com.datastax.oss.driver.api.core.data.CqlVector.newInstance(ary);
        } else if (value instanceof double[] da) {
            Double[] ary = new Double[da.length];
            for (int i = 0; i < da.length; i++) {
                ary[i]=da[i];
            }
            return com.datastax.oss.driver.api.core.data.CqlVector.newInstance(ary);
        } else {
            return (com.datastax.oss.driver.api.core.data.CqlVector) value;
        }

    }
}
