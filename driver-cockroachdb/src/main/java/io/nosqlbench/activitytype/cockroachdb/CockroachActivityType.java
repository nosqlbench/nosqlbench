package io.nosqlbench.activitytype.cockroachdb;

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


import io.nosqlbench.activitytype.jdbc.api.JDBCActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.annotations.Service;

@Service(value = ActivityType.class, selector = "cockroachdb")
public class CockroachActivityType implements ActivityType<CockroachActivity> {

    @Override
    public ActionDispenser getActionDispenser(CockroachActivity activity) {
        return new JDBCActionDispenser(activity);
    }

    @Override
    public CockroachActivity getActivity(ActivityDef activityDef) {
        return new CockroachActivity(activityDef);
    }
}
