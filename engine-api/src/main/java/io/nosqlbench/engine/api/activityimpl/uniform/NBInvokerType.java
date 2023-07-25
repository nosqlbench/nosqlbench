/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.activityimpl.uniform;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * If you provide a type of invokable element in this list, then it should
 * automatically be handled by NB.
 */
public enum NBInvokerType {
    NBRunnable(Runnable.class),
    NBCallable(Callable.class),
    NBFunction(Function.class);

    private final Class<?> typeclass;

    NBInvokerType(Class<?> typeClass) {
        this.typeclass=typeClass;
    }

    public static Optional<NBInvokerType> valueOfType(Class<?> c) {
        for (NBInvokerType type : NBInvokerType.values()) {
            if (type.typeclass.equals(c)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

}
