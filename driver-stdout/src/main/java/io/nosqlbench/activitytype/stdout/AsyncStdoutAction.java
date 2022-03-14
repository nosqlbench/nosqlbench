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

package io.nosqlbench.activitytype.stdout;

import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.core.BaseAsyncAction;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.StartedOp;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.TrackedOp;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.virtdata.core.templates.StringBindings;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.function.LongFunction;

@SuppressWarnings("Duplicates")
public class AsyncStdoutAction extends BaseAsyncAction<StdoutOpContext, StdoutActivity> {
    private final static Logger logger = LogManager.getLogger(AsyncStdoutAction.class);

    private OpSequence<StringBindings> sequencer;

    public AsyncStdoutAction(int slot, StdoutActivity activity) {
        super(activity, slot);
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);
        this.sequencer = activity.getOpSequence();
    }

    public StdoutOpContext allocateOpData(long cycle) {

        StdoutOpContext opc = new StdoutOpContext();
        try (Timer.Context bindTime = activity.bindTimer.time()) {
            opc.stringBindings = sequencer.apply(cycle);
            opc.statement = opc.stringBindings.bind(cycle);
            if (activity.getShowstmts()) {
                logger.info("STMT(cycle=" + cycle + "):\n" + opc.statement);
            }
        }
        return opc;
    }

    @Override
    public void startOpCycle(TrackedOp<StdoutOpContext> opc) {
        StartedOp<StdoutOpContext> started = opc.start();
        int result=0;
        try (Timer.Context executeTime = activity.executeTimer.time()) {
            activity.write(opc.getOpData().statement);
        } catch (Exception e) {
            result=1;
            started.fail(result);
            throw new RuntimeException("Error writing output:" + e, e);
        } finally {
            started.succeed(result);
        }
    }

    @Override
    public LongFunction<StdoutOpContext> getOpInitFunction() {
        return this::allocateOpData;
    }
}
