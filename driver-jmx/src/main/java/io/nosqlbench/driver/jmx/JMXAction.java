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

package io.nosqlbench.driver.jmx;

import io.nosqlbench.driver.jmx.ops.JmxOp;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class JMXAction implements SyncAction {

    private final static Logger logger = LogManager.getLogger(JMXAction.class);

    private final ActivityDef activityDef;
    private final int slot;
    private final JMXActivity activity;
    private OpSequence<OpDispenser<? extends JmxOp>> sequencer;

    public JMXAction(ActivityDef activityDef, int slot, JMXActivity activity) {
        this.activityDef = activityDef;
        this.slot = slot;
        this.activity = activity;
    }

    @Override
    public void init() {
        this.sequencer = activity.getSequencer();
    }

    @Override
    public int runCycle(long cycle) {
        LongFunction<? extends JmxOp> readyJmxOp = sequencer.apply(cycle);
        JmxOp jmxOp = readyJmxOp.apply(cycle);
        jmxOp.execute();
        return 0;
    }
}
