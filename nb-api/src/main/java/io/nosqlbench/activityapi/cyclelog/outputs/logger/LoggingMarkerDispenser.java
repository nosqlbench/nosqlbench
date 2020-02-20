/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.activityapi.cyclelog.outputs.logger;

import io.nosqlbench.activityapi.core.Activity;
import io.nosqlbench.activityapi.output.Output;
import io.nosqlbench.activityapi.output.OutputDispenser;
import io.virtdata.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service(OutputDispenser.class)
public class LoggingMarkerDispenser implements OutputDispenser {

    private final static Logger logger = LoggerFactory.getLogger(LoggingMarkerDispenser.class);
    private Activity activity;

    public LoggingMarkerDispenser(Activity activity) {
        this.activity = activity;
    }

    @Override
    public Output getOutput(long slot) {
        return new LoggingOutput(activity.getActivityDef(), slot);
    }

}
