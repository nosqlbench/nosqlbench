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
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.LongFunction;

@Categories(Category.state)
@ThreadSafeMapper
public class LoadCqlVectorFromArray implements LongFunction<CqlVector> {

    private final String name;
    private final Function<Object, Object> nameFunc;
    private final CqlVector[] defaultValue;
    private final int len;
    private final int batchsize;

    public LoadCqlVectorFromArray(String name, int len, int batchsize) {
        this.name = name;
        this.nameFunc = null;
        Float[] ary = new Float[len];
        for (int i = 0; i < len; i++) {
            ary[i] = (float)i;
        }
        this.defaultValue = new CqlVector[]{CqlVector.newInstance(ary)};
        this.len = len;
        this.batchsize = batchsize;
    }

    @Override
    public CqlVector apply(long cycle) {
        int offset = (int) (cycle % batchsize);
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        String varname = (nameFunc != null) ? String.valueOf(nameFunc.apply(cycle)) : name;
        Object object = map.getOrDefault(varname, defaultValue);
        if (object.getClass().isArray()) {
            object = Array.get(object,offset);
        } else if (object instanceof double[][] dary) {
            object = dary[offset];
        } else if (object instanceof float[][] fary) {
            object = fary[offset];
        } else if (object instanceof Double[][] dary) {
            object = dary[offset];
        } else if (object instanceof Float[][] fary) {
            object = fary[offset];
        } else if (object instanceof CqlVector<?>[] cary) {
            object = cary[offset];
        } else if (object instanceof List<?> list) {
            object = list.get(offset);
        } else {
            throw new RuntimeException("Unrecognized type for ary of ary:" + object.getClass().getCanonicalName());
        }

        if (object instanceof CqlVector<?> cqlvector) {
            return cqlvector;
        } else if (object instanceof float[] fa) {
            Float[] ary = new Float[fa.length];
            for (int i = 0; i < fa.length; i++) {
                ary[i] = fa[i];
            }
            return CqlVector.newInstance(ary);
        } else if (object instanceof double[] da) {
            Double[] ary = new Double[da.length];
            for (int i = 0; i < da.length; i++) {
                ary[i] = da[i];
            }
            return CqlVector.newInstance(ary);
        } else {
            return (CqlVector) object;
        }
    }
}
