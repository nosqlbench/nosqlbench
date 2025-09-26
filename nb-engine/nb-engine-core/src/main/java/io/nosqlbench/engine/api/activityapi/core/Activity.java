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

import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;

/**
 * Provides the components needed to build and run an activity at runtime.
 * This is now a concrete class that extends ActivityImpl for backward compatibility.
 *
 * @deprecated Use ActivityImpl directly instead. This class exists only for migration compatibility.
 */
@Deprecated(since = "5.0", forRemoval = true)
public class Activity extends ActivityImpl {

    /**
     * Constructor for backward compatibility.
     * Creates a new Activity using the ActivityImpl implementation.
     */
    public Activity(NBComponent parent, ActivityDef activityDef) {
        super(parent, activityDef);
    }

    /**
     * Alternative constructor for string-based activity definitions.
     */
    public Activity(NBComponent parent, String activityDefString) {
        this(parent, ActivityDef.parseActivityDef(activityDefString));
    }

}