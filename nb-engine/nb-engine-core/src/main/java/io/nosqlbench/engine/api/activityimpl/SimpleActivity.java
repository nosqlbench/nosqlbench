/*
 * Copyright (c) 2022-2024 nosqlbench
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

package io.nosqlbench.engine.api.activityimpl;

import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;

/**
 * A default implementation of an Activity, suitable for building upon.
 *
 * @deprecated Use Activity directly instead. This class exists only for migration compatibility.
 * All functionality has been moved to ActivityImpl.
 */
@Deprecated(since = "5.0", forRemoval = true)
public class SimpleActivity extends Activity {

    public SimpleActivity(NBComponent parent, ActivityDef activityDef) {
        super(parent, activityDef);
    }

    public SimpleActivity(NBComponent parent, String activityDefString) {
        super(parent, ActivityDef.parseActivityDef(activityDefString));
    }
}