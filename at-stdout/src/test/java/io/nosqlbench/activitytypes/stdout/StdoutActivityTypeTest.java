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

package io.nosqlbench.activitytypes.stdout;

import io.nosqlbench.activityapi.core.Action;
import io.nosqlbench.activityapi.core.ActionDispenser;
import io.nosqlbench.activityimpl.ActivityDef;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by sebastianestevez on 5/5/17.
 */
public class StdoutActivityTypeTest {
    @Test
    public void testDiagActivity() {
        StdoutActivityType stdoutAt = new StdoutActivityType();
        String atname = stdoutAt.getName();
        assertThat(atname.equals("stdout"));
        ActivityDef ad = ActivityDef.parseActivityDef("type=stdout; yaml=stdout-test;");
        StdoutActivity stdoutActivity = stdoutAt.getActivity(ad);
        ActionDispenser actionDispenser = stdoutAt.getActionDispenser(stdoutActivity);
        Action action = actionDispenser.getAction(1);
    }
}
