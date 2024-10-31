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

package io.nosqlbench.adapter.cqld4.opmappers;

import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapter.cqld4.*;
import io.nosqlbench.adapter.cqld4.opdispensers.Cqld4PreparedStmtDispenser;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4BaseOp;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlPreparedStatement;
import io.nosqlbench.adapter.cqld4.processors.CqlFieldCaptureProcessor;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.Space;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import io.nosqlbench.nb.api.config.params.ParamsParser;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.core.templates.ParsedTemplateString;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

public class CqlD4PreparedStmtMapper extends Cqld4CqlBaseOpMapper<Cqld4CqlPreparedStatement> {

    private final TypeAndTarget<CqlD4OpType, String> target;

    public CqlD4PreparedStmtMapper(Cqld4DriverAdapter adapter,
                                   TypeAndTarget<CqlD4OpType, String> target) {
        super(adapter);
        this.target = target;
    }

    @Override
    public OpDispenser<Cqld4CqlPreparedStatement> apply(ParsedOp op, LongFunction<Cqld4Space> spaceInitF) {
        ParsedTemplateString stmtTpl = op.getAsTemplate(target.field).orElseThrow(() -> new BasicError(
            "No statement was found in the op template:" + op
        ));

        RSProcessors processors = new RSProcessors();
        if (stmtTpl.getCaptures().size()>0) {
            processors.add(() -> new CqlFieldCaptureProcessor(stmtTpl.getCaptures()));
        }

        Optional<List> processorList = op.getOptionalStaticConfig("processors", List.class);

        processorList.ifPresent(l -> {
            l.forEach(m -> {
                Map<String, String> pconfig = ParamsParser.parseToMap(m, "type");
                ResultSetProcessor processor = Cqld4Processors.resolve(pconfig);
                processors.add(() -> processor);
            });
        });

        return new Cqld4PreparedStmtDispenser(adapter, op, stmtTpl, processors);
    }

}
