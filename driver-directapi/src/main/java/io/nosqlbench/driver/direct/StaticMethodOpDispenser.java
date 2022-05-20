package io.nosqlbench.driver.direct;

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


import io.nosqlbench.engine.api.activityimpl.OpDispenser;

import java.lang.reflect.Method;
import java.util.function.LongFunction;

public class StaticMethodOpDispenser implements OpDispenser<DirectCall> {
    private final LongFunction<Object[]> argsfunc;
    private final Method method;
    private final Object instance;

    public StaticMethodOpDispenser(Method method, Object instance, LongFunction<Object[]> argsfunc) {
        this.method = method;
        this.instance = instance;
        this.argsfunc = argsfunc;
    }

    @Override
    public DirectCall apply(long value) {
        Object[] args = argsfunc.apply(value);
        return new DirectCall(method, instance, args);
    }
}
