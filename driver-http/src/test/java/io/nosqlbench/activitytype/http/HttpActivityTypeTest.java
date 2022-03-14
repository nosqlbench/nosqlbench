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

package io.nosqlbench.activitytype.http;

import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import org.junit.jupiter.api.Test;

public class HttpActivityTypeTest {
    @Test
    public void testHttpActivity() {
        HttpActivityType httpAt = new HttpActivityType();

        ActivityDef ad = ActivityDef.parseActivityDef("driver=http; yaml=http-google.yaml; port=80; cycles=1;");
        HttpActivity httpActivity = httpAt.getActivity(ad);
        httpActivity.initActivity();
        ActionDispenser actionDispenser = httpAt.getActionDispenser(httpActivity);
        Action action = actionDispenser.getAction(1);
    }
}
