package io.nosqlbench.driver.jms;

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

import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

@Service(value = ActivityType.class, selector = "s4j")
public class S4JActivityType implements ActivityType<S4JActivity> {
    @Override
    public ActionDispenser getActionDispenser(S4JActivity activity) {
        return new S4JActionDispenser(activity);
    }

    @Override
    public S4JActivity getActivity(ActivityDef activityDef) {
        return new S4JActivity(activityDef);
    }

    private static class S4JActionDispenser implements ActionDispenser {
        private final S4JActivity activity;
        public S4JActionDispenser(S4JActivity activity) {
            this.activity = activity;
        }

        @Override
        public Action getAction(int slot) {
            return new S4JAction(activity, slot);
        }
    }
}
