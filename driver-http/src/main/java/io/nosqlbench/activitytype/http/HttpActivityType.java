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

package io.nosqlbench.activitytype.http;

import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service(value = ActivityType.class, selector = "http")
public class HttpActivityType implements ActivityType<HttpActivity> {

    private static final Logger logger = LogManager.getLogger(HttpActivityType.class);

    @Override
    public ActionDispenser getActionDispenser(HttpActivity activity) {
        if (activity.getParams().getOptionalString("async").isPresent()) {
            throw new RuntimeException("The async http driver is not online yet.");
        }
        return new HttpActionDispenser(activity);
    }

    @Override
    public HttpActivity getActivity(ActivityDef activityDef) {
        return new HttpActivity(activityDef);
    }
}
