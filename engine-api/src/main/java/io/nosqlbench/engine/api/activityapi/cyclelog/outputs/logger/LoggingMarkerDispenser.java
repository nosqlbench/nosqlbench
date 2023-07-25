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

package io.nosqlbench.engine.api.activityapi.cyclelog.outputs.logger;

import io.nosqlbench.engine.api.activityapi.output.Output;
import io.nosqlbench.engine.api.activityapi.output.OutputDispenser;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service(value = OutputDispenser.class, selector = "logging-marker")
public class LoggingMarkerDispenser implements OutputDispenser {

    private final static Logger logger = LogManager.getLogger(LoggingMarkerDispenser.class);
    private final Activity activity;

    public LoggingMarkerDispenser(Activity activity) {
        this.activity = activity;
    }

    @Override
    public Output getOutput(long slot) {
        return new LoggingOutput(activity.getActivityDef(), slot);
    }

}
