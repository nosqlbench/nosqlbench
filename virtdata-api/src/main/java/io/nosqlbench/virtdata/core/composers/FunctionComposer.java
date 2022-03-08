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

package io.nosqlbench.virtdata.core.composers;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.DataMapperFunctionMapper;
import io.nosqlbench.virtdata.core.bindings.ResolvedFunction;

public interface FunctionComposer<T> {

    Object getFunctionObject();

    FunctionComposer andThen(Object outer);

    default ResolvedFunction getResolvedFunction() {
        return new ResolvedFunction(
                getFunctionObject(),
                getFunctionObject().getClass().getAnnotation(ThreadSafeMapper.class) != null,
                null, null,
                null, null
        );
    }

    default ResolvedFunction getResolvedFunction(boolean isThreadSafe) {
        return new ResolvedFunction(
                getFunctionObject(),
                isThreadSafe,
                null, null,
                null, null
        );
    }

    default <R> DataMapper<R> getDataMapper() {
        return DataMapperFunctionMapper.map(getFunctionObject());
    }

}
