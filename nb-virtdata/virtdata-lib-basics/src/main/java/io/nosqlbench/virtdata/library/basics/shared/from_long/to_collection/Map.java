/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.api.annotations.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

/**
 * Create a {@code Map} from a long input based on three functions,
 * the first to determine the map size, and the second to populate
 * the map with key objects, and the third to populate the map with
 * value objects. The long input fed to the second and third functions
 * is incremented between entries.
 *
 * To directly create Maps with key and value Strings using the same
 * mapping functions, simply use {@link StringMap} instead.
 */
@Categories({Category.collections})
@ThreadSafeMapper
public class Map implements LongFunction<java.util.Map<Object, Object>> {

    private final LongToIntFunction sizeFunc;
    private final LongFunction[] keyFuncs;
    private final LongFunction[] valueFuncs;
    private final Mode mode;

    @Example({"Map(HashRange(3,7),NumberNameToString(),HashRange(1300,1700))",
            "create a map of size 3-7 entries, with a key of type " +
                    "string and a value of type int (Integer by autoboxing)"})
    public Map(LongToIntFunction sizeFunc,
               LongFunction<Object> keyFunc,
               LongFunction<Object> valueFunc) {
        this.mode = Mode.VarSized;

        this.sizeFunc = sizeFunc;
        this.keyFuncs = new LongFunction[1];
        keyFuncs[0]=keyFunc;
        this.valueFuncs = new LongFunction[1];
        valueFuncs[0]=valueFunc;

    }

    @Example({"Map(NumberNameToString(),HashRange(1300,1700),NumberNameToString(),HashRange(3,7))",
            "create a map of size 2, with a specific function for each key and each value"})
    @SafeVarargs
    public Map(LongFunction<Object>... objfuncs) {
        if ((objfuncs.length%2)!=0) {
            Object testValue = objfuncs[0].apply(0L);
            if (testValue instanceof Number n) {
                LongFunction<Object>[] finalObjfuncs = objfuncs;
                this.sizeFunc= l -> ((Number) finalObjfuncs[0].apply(l)).intValue();
                objfuncs = Arrays.copyOfRange(objfuncs, 1, objfuncs.length);
                this.mode=Mode.VarSized;
            } else {
                throw new BasicError("An even number of functions must be provided, unless "
                                     + "the first one produces a numeric value.");
            }
        } else {
            this.mode = Mode.Tuples;
            int size = objfuncs.length/2;
            sizeFunc=(l) -> size;
        }
        int size = objfuncs.length / 2;
        keyFuncs = new LongFunction[size];
        valueFuncs = new LongFunction[size];
        for (int i = 0; i < size; i++) {
            keyFuncs[i]=objfuncs[i<<1];
            valueFuncs[i] = objfuncs[(i<<1)+1];
        }
    }

    @Override
    public java.util.Map<Object, Object> apply(long value) {
        int size = sizeFunc.applyAsInt(value);
        HashMap<Object, Object> map = new HashMap<>(size);
        switch (mode) {
            case VarSized:
                for (int i = 0; i < size; i++) {
                    Object keyObject = keyFuncs[0].apply(value + i);
                    Object valueObject = valueFuncs[0].apply(value + i);
                    map.put(keyObject, valueObject);
                }
                break;
            case Tuples:
                for (int i = 0; i < keyFuncs.length; i++) {
                    Object keyObject = keyFuncs[i].apply(value +i);
                    Object valueObject = valueFuncs[i].apply(value+i);
                    map.put(keyObject,valueObject);
                }
                break;
        }
        return map;
    }

    private enum Mode {
        VarSized,
        Tuples
    }
}
