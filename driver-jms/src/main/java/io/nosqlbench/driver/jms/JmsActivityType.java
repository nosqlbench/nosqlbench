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

package io.nosqlbench.driver.jms;

import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

@Service(value = ActivityType.class, selector = "jms")
public class JmsActivityType implements ActivityType<JmsActivity> {
    @Override
    public ActionDispenser getActionDispenser(JmsActivity activity) {
        return new PulsarJmsActionDispenser(activity);
    }

    @Override
    public JmsActivity getActivity(ActivityDef activityDef) {
        return new JmsActivity(activityDef);
    }

    private static class PulsarJmsActionDispenser implements ActionDispenser {
        private final JmsActivity activity;
        public PulsarJmsActionDispenser(JmsActivity activity) {
            this.activity = activity;
        }

        @Override
        public Action getAction(int slot) {
            return new JmsAction(activity, slot);
        }
    }
}
