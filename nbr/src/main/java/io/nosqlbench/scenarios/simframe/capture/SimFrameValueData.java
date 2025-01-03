/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.scenarios.simframe.capture;

import io.nosqlbench.engine.api.activityimpl.uniform.Activity;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricGauge;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricHistogram;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricTimer;

public class SimFrameValueData extends SimFrameCapture {
    public SimFrameValueData(Activity activity) {
        NBMetricTimer result_timer = activity.find().timer("name:result");
        NBMetricTimer result_success_timer = activity.find().timer("name:result_success");
        NBMetricGauge cyclerate_gauge = activity.find().gauge("name=config_cyclerate");
        NBMetricHistogram tries_histo_src = activity.find().histogram("name=tries");
        NBMetricHistogram tries_histo = tries_histo_src.attachHdrDeltaHistogram();

        addDirect("target_rate",
                cyclerate_gauge::getValue,
            Double.NaN);
        addDeltaTime("achieved_oprate",
                result_timer::getCount,
            Double.NaN);
        addDeltaTime("achieved_ok_oprate",
                result_success_timer::getCount
            , 1.0);

        addRemix("achieved_success_ratio", vars -> {
            // exponentially penalize results which do not attain 100% successful op rate
            double achieved_oprate = vars.get("achieved_oprate");
            if (achieved_oprate==0d) {
                return 0d;
            }
            double basis = Math.min(1.0d, vars.get("achieved_ok_oprate") / achieved_oprate);
            return Math.pow(basis, 3);
        });
        addRemix("achieved_target_ratio", (vars) -> {
            // exponentially penalize results which do not attain 100% target rate
            double target_rate = vars.get("target_rate");
            if (target_rate==0d) {
                return 0;
            }
            double basis = Math.min(1.0d, vars.get("achieved_ok_oprate") / vars.get("target_rate"));
            return Math.pow(basis, 3);
        });
    }
}
