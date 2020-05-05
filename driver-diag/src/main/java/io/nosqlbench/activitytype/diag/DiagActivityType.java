/*
/*
*   Copyright 2016 jshook
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/
package io.nosqlbench.activitytype.diag;

import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DiagActivity, aka "diag", is simply a diagnostic activity.
 * It logs the input to priority INFO on some interval, in milliseconds.
 * Each interval, one of the diag actions will report both the current input value and
 * the number of milliseconds that have elapsed since the activity was scheduled to report.
 *
 * Diag serves as a basic template for implementing your own activity type.
 */
@Service(ActivityType.class)
public class DiagActivityType implements ActivityType<DiagActivity> {

    private static final Logger logger = LoggerFactory.getLogger(DiagActivityType.class);

    @Override
    public String getName() {
        return "diag";
    }

    @Override
    public ActionDispenser getActionDispenser(DiagActivity activity) {
        return new DiagActionDispenser(activity);
    }

    @Override
    public DiagActivity getActivity(ActivityDef activityDef) {
        return new DiagActivity(activityDef);
    }
}
