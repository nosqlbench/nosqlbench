package io.nosqlbench.adapter.cqld4;

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


import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapter.cqld4.opdispensers.CqlD4PreparedBatchOpDispenser;
import io.nosqlbench.adapter.cqld4.opdispensers.Cqld4BatchStatementDispenser;
import io.nosqlbench.adapter.cqld4.opdispensers.Cqld4PreparedStmtDispenser;
import io.nosqlbench.adapter.cqld4.opdispensers.Cqld4SimpleCqlStmtDispenser;
import io.nosqlbench.adapter.cqld4.processors.CqlFieldCaptureProcessor;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.nb.api.config.params.ParamsParser;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Cqld4OpMapper implements OpMapper<Cqld4Op> {


    private final DriverSpaceCache<? extends Cqld4Space> cache;
    private final NBConfiguration cfg;

    public Cqld4OpMapper(NBConfiguration config, DriverSpaceCache<? extends Cqld4Space> cache) {
        this.cfg = config;
        this.cache = cache;
    }

    public OpDispenser<Cqld4Op> apply(ParsedOp cmd) {

        ParsedTemplate stmtTpl = cmd.getStmtAsTemplate().orElseThrow(() -> new BasicError(
            "No statement was found in the op template:" + cmd
        ));

        RSProcessors processors = new RSProcessors();
        if (stmtTpl.getCaptures().size()>0) {
            processors.add(() -> new CqlFieldCaptureProcessor(stmtTpl.getCaptures()));
        }

//        cmd.getOptionalStaticConfig("processor",String.class)
//            .map(s -> ParamsParser.parseToMap(s,"type"))
//            .map(Cqld4Processors::resolve)
//            .ifPresent(processors::add);
//
        Optional<List> processorList = cmd.getOptionalStaticConfig("processors", List.class);
        processorList.ifPresent(l -> {
            l.forEach(m -> {
                Map<String, String> pconfig = ParamsParser.parseToMap(m, "type");
                ResultSetProcessor processor = Cqld4Processors.resolve(pconfig);
                processors.add(() -> processor);
            });
        });
//
//            processorList.stream()
//            .map(s -> ParamsParser.parseToMap(s,"type"))
//            .map(Cqld4Processors::resolve)
//            .forEach(processors::add);

        Cqld4Space cqld4Space = cache.get(cmd.getStaticConfigOr("space", "default"));
        boolean prepared = cmd.getStaticConfigOr("prepared", true);
        boolean batch = cmd.getStaticConfigOr("boolean", false);
        CqlSession session = cqld4Space.getSession();



        if (prepared && batch) {
            return new CqlD4PreparedBatchOpDispenser(session, cmd);
        } else if (prepared) {
            return new Cqld4PreparedStmtDispenser(session, cmd, processors);
        } else if (batch) {
            return new Cqld4BatchStatementDispenser(session, cmd);
        } else {
            return new Cqld4SimpleCqlStmtDispenser(session, cmd);
        }
    }

}
