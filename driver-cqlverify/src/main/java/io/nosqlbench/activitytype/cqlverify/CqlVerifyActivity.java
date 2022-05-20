package io.nosqlbench.activitytype.cqlverify;

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


import io.nosqlbench.activitytype.cql.core.CqlActivity;
import io.nosqlbench.activitytype.cql.statements.rsoperators.AssertSingleRowResultSet;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.ParameterMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This activity is just a thin wrapper at this point.
 * Most of the functionality it used to have has been
 * generalized into the cql activity proper at this point.
 */
public class CqlVerifyActivity extends CqlActivity {

    private final static Logger logger = LogManager.getLogger(CqlVerifyActivity.class);

    public CqlVerifyActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public synchronized void initActivity() {

        ParameterMap activityParams = super.getActivityDef().getParams();
        if (!activityParams.containsKey("verify") &&
            !activityParams.containsKey("verify-fields")) {
            logger.info("Pre-configuring activity param 'verify=*' since none was provided.");
            logger.info("To control this on a per-statement basis, use the verify param.");
            activityParams.put("verify", "*");
        }

        if (!activityParams.containsKey("compare")) {
            activityParams.put("compare", "all");
            logger.info("Pre-configuring activity param 'compare=all' since none was provided.");
            logger.info("To control this on a per-statement basis, use the compare param.");
        }

        super.initActivity();
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);
        addResultSetCycleOperator(new AssertSingleRowResultSet());
    }
}
