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

package io.nosqlbench.activitytype.stdout;

import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

import java.util.Optional;

@Service(value= ActivityType.class, selector="stdout-nb4")
public class StdoutActivityType implements ActivityType<StdoutActivity> {

    @Override
    public StdoutActivity getActivity(ActivityDef activityDef) {

        // sanity check that we have a yaml parameter, which contains our statements and bindings
        Optional<String> stmtsrc = activityDef.getParams().getOptionalString("op", "stmt", "statement", "yaml", "workload");
        if (stmtsrc.isEmpty()) {
            throw new RuntimeException("Without a workload or op parameter, there is nothing to do. (Add a workload (yaml file) or an op= template, like" +
                " op='cycle={{Identity()}}'");
        }

        return new StdoutActivity(activityDef);
    }

    @Override
    public ActionDispenser getActionDispenser(StdoutActivity activity) {
        return new StdoutActionDispenser(activity);
    }

    private static class StdoutActionDispenser implements ActionDispenser {

        private final StdoutActivity activity;

        private StdoutActionDispenser(StdoutActivity activity) {
            this.activity = activity;
        }

        @Override
        public Action getAction(int slot) {
            if (activity.getActivityDef().getParams().getOptionalString("async").isPresent()) {
                return new AsyncStdoutAction(slot, activity);
            }
            return new StdoutAction(slot, activity);
        }
    }
}
