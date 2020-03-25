package io.nosqlbench.activitytype.diag;

import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import org.testng.annotations.Test;

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
public class DiagActivityTypeTest {

    @Test
    public void testDiagActivity() {
        DiagActivityType da = new DiagActivityType();
        da.getName();
        ActivityDef ad = ActivityDef.parseActivityDef("driver=diag;");
        DiagActivity a = da.getActivity(ad);
        a.initActivity();

        ActionDispenser actionDispenser = da.getActionDispenser(a);
        Action action = actionDispenser.getAction(1);
        ((SyncAction)action).runCycle(1L);
    }

}
