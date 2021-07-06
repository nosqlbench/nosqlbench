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

package io.nosqlbench.activitytype.stdout;

import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.virtdata.core.templates.StringBindings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("Duplicates")
public class StdoutAction implements SyncAction {

    private static final Logger logger = LogManager.getLogger(StdoutAction.class);
    private final int slot;
    private final StdoutActivity activity;
    private final int maxTries = 10;
    private boolean showstmts;
    private OpSequence<StringBindings> opsource;

    public StdoutAction(int slot, StdoutActivity activity) {
        this.slot = slot;
        this.activity = activity;
    }

    @Override
    public void init() {
        this.opsource = activity.getOpSequence();
    }

    @Override
    public int runCycle(long cycle) {
        StringBindings stringBindings;
        String statement = null;
        try (Timer.Context bindTime = activity.bindTimer.time()) {
            stringBindings = opsource.apply(cycle);
            statement = stringBindings.bind(cycle);
            showstmts = activity.getShowstmts();
            if (showstmts) {
                logger.info("STMT(cycle=" + cycle + "):\n" + statement);
            }
        }

        try (Timer.Context executeTime = activity.executeTimer.time()) {
            activity.write(statement);
        } catch (Exception e) {
            throw new RuntimeException("Error writing output:" + e, e);
        }
        return 0;
    }

}
