package com.datastax.ebdrivers.cql;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.activitytype.cql.core.CqlAction;
import io.nosqlbench.activitytype.cql.core.CqlActivity;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class CqlActionTest {

    @Test
    @Disabled
    public void testCqlAction() {
        ActivityDef ad = ActivityDef.parseActivityDef("driver=ebdrivers;alias=foo;yaml=write-telemetry.yaml;");
        CqlActivity cac = new CqlActivity(ad);
        CqlAction cq = new CqlAction(ad, 0, cac);
        cq.init();
        cq.runCycle(5);
    }


}
