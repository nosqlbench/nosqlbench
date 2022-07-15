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

package io.nosqlbench.driver.pulsar;

import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

@Service(value= ActivityType.class, selector="pulsar")
public class PulsarActivityType implements ActivityType<PulsarActivity> {

    @Override
    public ActionDispenser getActionDispenser(PulsarActivity activity) {
        if (activity.getParams().getOptionalString("async").isPresent()) {
            throw new RuntimeException("The async pulsar driver is not implemented yet.");
        }
        return new PulsarActionDispenser(activity);
    }

    @Override
    public PulsarActivity getActivity(ActivityDef activityDef) {
        return new PulsarActivity(activityDef);
    }

    private static class PulsarActionDispenser implements ActionDispenser {
        private final PulsarActivity activity;
        public PulsarActionDispenser(PulsarActivity activity) {
            this.activity = activity;
        }

        @Override
        public Action getAction(int slot) {
            return new PulsarAction(activity, slot);
        }
    }
}
