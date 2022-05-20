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


import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;

import java.lang.reflect.Method;

public class DirectCall implements Op,Runnable {
    private final Method method;
    private final Object[] args;
    private final Object instance;

    public DirectCall(Method method, Object instance, Object[] args) {
        this.method = method;
        this.instance = instance;
        this.args = args;
    }

    @Override
    public void run() {
        try {
            method.invoke(instance,args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
