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

package io.nosqlbench.engine.api.activityimpl.action;

import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class CoreAction implements SyncAction {
    private final static Logger logger = LogManager.getLogger(CoreAction.class);

    private final int interval;
    private final int slot;
    private final ActivityDef activityDef;

    public CoreAction(ActivityDef activityDef, int slot) {
        this.activityDef = activityDef;
        this.slot = slot;
        this.interval = activityDef.getParams().getOptionalInteger("interval").orElse(1000);
    }

    @Override
    public int runCycle(long cycle) {
        if ((cycle % interval) == 0) {
            logger.info(() -> activityDef.getAlias() + "[" + slot + "]: cycle=" + cycle);
        } else {
            logger.trace(() -> activityDef.getAlias() + "[" + slot + "]: cycle=" + cycle);
        }
        return 0;
    }
}
