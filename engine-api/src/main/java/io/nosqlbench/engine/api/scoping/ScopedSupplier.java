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

package io.nosqlbench.engine.api.scoping;

import java.util.function.Supplier;

/**
 * Helper class to consolidate the notions of scoping items over different
 * NB layers of execution.
 */
public enum ScopedSupplier {

    /**
     * Once a supplier is instantiated for singleton scope, it should provide only one instance of
     * the supplied element for that instance. Multiple instances (separately constructed)
     * of the supplier can each provide their own distinct supplied instance.
     */
    singleton,
    /**
     * Once a supplier is instantiated for thread scope, it should provide a unique instance of
     * the supplied element for each thread, according to the inner supplier's semantics.
     */
    thread,
    /**
     * When a supplier is instantiated for caller scope, each time the supplier is accessed,
     * it returns a new instance according to the inner supplier's semantics.
     */
    caller;

    public <T> Supplier<T> supplier(Supplier<T> supplier) {

        switch (this) {
            case singleton:
                T got = supplier.get();
                return () -> got;
            case thread:
                ThreadLocal<T> tlsupplier = ThreadLocal.withInitial(supplier);
                return tlsupplier::get;
            case caller:
                return supplier;
            default:
                throw new RuntimeException("Unknown scope type:" + this);
        }
    }

}
