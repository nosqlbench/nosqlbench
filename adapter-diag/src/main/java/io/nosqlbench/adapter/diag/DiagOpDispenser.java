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

package io.nosqlbench.adapter.diag;

import io.nosqlbench.adapter.diag.optasks.DiagTask;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.NBReconfigurable;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateLimiter;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.annotations.ServiceSelector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.LongFunction;

public class DiagOpDispenser extends BaseOpDispenser<DiagOp,DiagSpace> implements NBReconfigurable {
    private final static Logger logger = LogManager.getLogger(DiagOpDispenser.class);
    private OpFunc opFunc;

    private RateLimiter diagRateLimiter;
    private LongFunction<DiagSpace> spaceF;
    private OpFunc opFuncs;

    public DiagOpDispenser(DiagDriverAdapter adapter, LongFunction<DiagSpace> spaceF, ParsedOp op) {
        super(adapter,op);
        this.opFunc = resolveOpFunc(spaceF, op);
    }

    private OpFunc resolveOpFunc(LongFunction<DiagSpace> spaceF, ParsedOp op) {
        List<DiagTask> tasks = new ArrayList<>();
        Set<String> tasknames = op.getDefinedNames();

        /**
         * Dynamically load diag tasks and add them to the in-memory template used by the op dispenser
         */
        for (String taskname : tasknames) {

            // Get the value of the keyed task name, but throw an error if it is not static or not a map

            // Get the op config by name, if provided in string or map form, and
            // produce a normalized map form which contains the type field. If
            // the type isn't contained in the parsed form, inject the name as short-hand
            // Also, inject the name into the map
            Map<String,Object> taskcfg = op.parseStaticCmdMap(taskname, "type");
            taskcfg.computeIfAbsent("name",l -> taskname);
            taskcfg.computeIfAbsent("type",l -> taskname);
            String optype = taskcfg.remove("type").toString();
            String opname = taskcfg.get("name").toString();

            // Dynamically load the named task instance, based on the op field key AKA the taskname
            // and ensure that exactly one is found or throw an error
            DiagTask task = ServiceSelector.of(optype, ServiceLoader.load(DiagTask.class)).getOne();
            task.setLabelsFrom(op);
            task.setName(opname);

            // Load the configuration model of the dynamically loaded task for type-safe configuration
            NBConfigModel cfgmodel = task.getConfigModel();

            // Apply the raw configuration data to the configuration model, which
            // both validates the provided configuration fields and
            // yields a usable configuration that should apply to the loaded task without error or ambiguity
            NBConfiguration taskconfig = cfgmodel.apply(taskcfg);

            // Apply the validated configuration to the loaded task
            task.applyConfig(taskconfig);

            // Store the task into the diag op's list of things to do when it runs
            tasks.add(task);
        }
        this.opFunc = new OpFunc(spaceF,tasks);
        return opFunc;
    }

    @Override
    public void applyReconfig(NBConfiguration recfg) {
        opFunc.applyReconfig(recfg);
    }

    @Override
    public NBConfigModel getReconfigModel() {
        return opFunc.getReconfigModel();
    }

    private final static class OpFunc implements LongFunction<DiagOp>, NBReconfigurable {
        private final List<DiagTask> tasks;
        private final LongFunction<DiagSpace> spaceF;

        public OpFunc(LongFunction<DiagSpace> spaceF, List<DiagTask> tasks) {
            this.tasks = tasks;
            this.spaceF = spaceF;
        }

        @Override
        public DiagOp apply(long value) {
            DiagSpace space = spaceF.apply(value);
            return new DiagOp(space, tasks);
        }

        @Override
        public void applyReconfig(NBConfiguration recfg) {
            NBReconfigurable.applyMatching(recfg,tasks);
        }

        @Override
        public NBConfigModel getReconfigModel() {
            return NBReconfigurable.collectModels(DiagTask.class, tasks);
        }
    }

    @Override
    public DiagOp apply(long value) {
        return opFunc.apply(value);
    }
}
