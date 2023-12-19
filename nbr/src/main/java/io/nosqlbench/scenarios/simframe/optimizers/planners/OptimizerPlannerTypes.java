/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.scenarios.simframe.optimizers.planners;

import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.scenarios.simframe.optimizers.planners.findmax.FindmaxPlanner;
import io.nosqlbench.scenarios.simframe.optimizers.planners.ratchet.RatchetPlanner;
import io.nosqlbench.scenarios.simframe.optimizers.planners.rcurve.RCurvePlanner;
import io.nosqlbench.scenarios.simframe.planning.SimFramePlanner;

public enum OptimizerPlannerTypes {
    //    survey,
    ratchet,
    findmax,
    rcurve,
    ;
    public SimFramePlanner<?,?> createPlanner(NBBaseComponent parent, NBCommandParams params) {
        return switch (this) {
            case findmax -> new FindmaxPlanner(parent, params);
            case rcurve -> new RCurvePlanner(parent, params);
            case ratchet -> new RatchetPlanner(parent, params);
        };
    }
}
