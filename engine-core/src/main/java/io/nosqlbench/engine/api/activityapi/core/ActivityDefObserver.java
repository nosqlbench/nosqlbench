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
package io.nosqlbench.engine.api.activityapi.core;

import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;

/**
 * Decorator interface for getting notified when an activities parameters are changed at runtime.
 *
 * This can be optionally implemented by Any Motor, Input, or Action. The eventing is mediated
 * through the ActivityExecutor in order to isolate the programmatic API from the internal API.
 */
public interface ActivityDefObserver {
    void onActivityDefUpdate(ActivityDef activityDef);

    static void apply(ActivityDef def, Object... candidates) {
        for (Object candidate : candidates) {
            if (candidate instanceof ActivityDefObserver observer) {
                observer.onActivityDefUpdate(def);
            }
        }
    }
}
