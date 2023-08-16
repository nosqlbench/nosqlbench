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

import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.NBReconfigurable;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.LongFunction;

public class DiagOpMapper implements OpMapper<DiagOp>, NBReconfigurable {
    private final Map<String,DiagOpDispenser> dispensers = new LinkedHashMap<>();
    private final DiagDriverAdapter adapter;

    public DiagOpMapper(DiagDriverAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public OpDispenser<? extends DiagOp> apply(ParsedOp op) {
        LongFunction<DiagSpace> spaceF = adapter.getSpaceFunc(op);
        DiagOpDispenser dispenser = new DiagOpDispenser(adapter,spaceF,op);
        dispensers.put(op.getName(),dispenser);
        return dispenser;
    }


    @Override
    public void applyReconfig(NBConfiguration recfg) {
        NBReconfigurable.applyMatching(recfg, dispensers.values());
    }

    @Override
    public NBConfigModel getReconfigModel() {
        return NBReconfigurable.collectModels(this.getClass(),new ArrayList<>(dispensers.values()));
    }
}
