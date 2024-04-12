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
import java.util.function.LongUnaryOperator;

/**
 * Clears the per-thread map which is used by the Expr function.
 */
@ThreadSafeMapper
@Categories({Category.state})
public class Clear implements LongUnaryOperator {

    private final String[] names;

    /**
     * Clear all named entries from the per-thread map.
     */
    @Example({"Clear()","clear all thread-local variables"})
    public Clear() {
        this.names=null;
    }

    /**
     * Clear the specified names from the per-thread map.
     * @param names The names to be removed from the map.
     */
    @Example({"Clear('foo')","clear the thread-local variable 'foo'"})
    @Example({"Clear('foo','bar')","clear the thread-local variables 'foo' and 'bar'"})
    public Clear(String... names) {
        this.names = names;
    }

    @Override
    public long applyAsLong(long operand) {

        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        if (names==null) {
            map.clear();
            return operand;
        }
        for (String name : names) {
            map.remove(name);
        }

        return operand;
    }
}
