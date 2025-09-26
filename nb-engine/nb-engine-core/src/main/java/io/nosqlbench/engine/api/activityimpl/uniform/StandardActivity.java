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

import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;

/**
 * This is a typed activity which is expected to become the standard
 * core of all new activity types. Extant NB drivers should also migrate
 * to this when possible.
 *
 * @param <R>
 *     A type of runnable which wraps the operations for this type of driver.
 * @param <S>
 *     The context type for the activity, AKA the 'space' for a named driver instance and its associated object graph
 *
 * @deprecated Use ActivityImpl directly instead. This class exists only for migration compatibility.
 * All functionality has been moved to ActivityImpl.
 */
@Deprecated(since = "5.0", forRemoval = true)
public class StandardActivity<R extends java.util.function.LongFunction, S> extends SimpleActivity {

    public StandardActivity(NBComponent parent, ActivityDef activityDef) {
        super(parent, activityDef);
    }
}