package io.nosqlbench.adapter.cqld4.opmappers;

import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapter.cqld4.Cqld4Processors;
import io.nosqlbench.adapter.cqld4.RSProcessors;
import io.nosqlbench.adapter.cqld4.ResultSetProcessor;
import io.nosqlbench.adapter.cqld4.opdispensers.Cqld4PreparedStmtDispenser;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.adapter.cqld4.processors.CqlFieldCaptureProcessor;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.nb.api.config.params.ParamsParser;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

public class CqlD4PreparedStmtMapper implements OpMapper<Cqld4CqlOp> {

    private final LongFunction<CqlSession> sessionFunc;
    private final TypeAndTarget<CqlD4OpType, String> target;

    public CqlD4PreparedStmtMapper(LongFunction<CqlSession> sessionFunc, TypeAndTarget<CqlD4OpType,String> target) {
        this.sessionFunc=sessionFunc;
        this.target = target;
    }

    public OpDispenser<Cqld4CqlOp> apply(ParsedOp cmd) {

        ParsedTemplate stmtTpl = cmd.getAsTemplate(target.field).orElseThrow(() -> new BasicError(
            "No statement was found in the op template:" + cmd
        ));

        RSProcessors processors = new RSProcessors();
        if (stmtTpl.getCaptures().size()>0) {
            processors.add(() -> new CqlFieldCaptureProcessor(stmtTpl.getCaptures()));
        }

        Optional<List> processorList = cmd.getOptionalStaticConfig("processors", List.class);

        processorList.ifPresent(l -> {
            l.forEach(m -> {
                Map<String, String> pconfig = ParamsParser.parseToMap(m, "type");
                ResultSetProcessor processor = Cqld4Processors.resolve(pconfig);
                processors.add(() -> processor);
            });
        });

        return new Cqld4PreparedStmtDispenser(sessionFunc, cmd, stmtTpl, processors);

    }
}
