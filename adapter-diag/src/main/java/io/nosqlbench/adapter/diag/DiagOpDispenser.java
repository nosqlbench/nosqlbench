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

package io.nosqlbench.adapter.diag;

import io.nosqlbench.adapter.diag.optasks.DiagOpTask;
import io.nosqlbench.nb.annotations.ServiceSelector;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateLimiter;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.nb.api.config.params.NBParams;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.LongFunction;

public class DiagOpDispenser extends BaseOpDispenser<DiagOp> {
    private final static Logger logger = LogManager.getLogger(DiagOpDispenser.class);
    private final LongFunction<DiagOp> opFunc;

    private RateLimiter diagRateLimiter;

    public DiagOpDispenser(ParsedOp op) {
        super(op);
        this.opFunc = resolveOpFunc(op);
    }

    private LongFunction<DiagOp> resolveOpFunc(ParsedOp op) {
        List<DiagOpTask> tasks = new ArrayList<>();
        Set<String> tasknames = op.getDefinedNames();

        /**
         * Dynamically load diag tasks and add them to the in-memory template used by the op dispenser
         */
        for (String taskname : tasknames) {
            // Get the value of the keyed task name, but throw an error if it is not static or not a map
            Object taskcfg = op.getStaticValue(taskname, Object.class);
            // Load this value into a map using the adaptive loading logic of NBParams
            // This can be a map or a string or a list.
            // Exactly one instance is required, and we take the field values from it as a map
            Map<String, Object> cfgmap = NBParams.one(taskcfg).getMap();
            // Dynamically load the named task instance, based on the op field key AKA the taskname
            // and ensure that exactly one is found or throw an error
            DiagOpTask task = ServiceSelector.of(taskname, ServiceLoader.load(DiagOpTask.class)).getOne();
            // Load the configuration model of the dynamically loaded task for type-safe configuration
            NBConfigModel cfgmodel = task.getConfigModel();
            // Apply the raw configuration data to the configuration model, which
            // both validates the provided configuration fields and
            // yields a usable configuration that should apply to the loaded task without error or ambiguity
            NBConfiguration taskconfig = cfgmodel.apply(cfgmap);
            // Apply the validated configuration to the loaded task
            task.applyConfig(taskconfig);
            // Store the task into the diag op's list of things to do when it runs
            tasks.add(task);
        }
        return l -> new DiagOp(tasks);
    }


    @Override
    public DiagOp apply(long value) {
        return opFunc.apply(value);
    }


}
