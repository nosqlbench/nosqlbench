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

package io.nosqlbench.engine.core.lifecycle.activity;

import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityimpl.uniform.StandardActivityType;
import io.nosqlbench.engine.core.lifecycle.scenario.Scenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Consolidates the activity type and activity instantiation logic into one place
 * per scope. Within the lifetime of this ActivityLoader, all activities may
 * see each other by name.
 */
public class ActivityLoader {
    private final static Logger logger = LogManager.getLogger("ACTIVITIES");
    private final Map<String, Activity> activityMap = new ConcurrentHashMap<>();
    private final Scenario scenario;

    public ActivityLoader(Scenario scenario) {
        this.scenario = scenario;
    }

    public synchronized Activity loadActivity(ActivityDef activityDef) {
        activityDef= activityDef.deprecate("yaml","workload").deprecate("type","driver");
        Activity activity = new StandardActivityType(activityDef).getAssembledActivity(activityDef, activityMap);
        activityMap.put(activity.getAlias(),activity);
        logger.debug("Resolved activity for alias '" + activityDef.getAlias() + "'");
        return activity;
    }

    public void purgeActivity(String activityAlias) {
        this.activityMap.remove(activityAlias);
    }
}
