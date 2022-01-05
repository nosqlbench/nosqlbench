package io.nosqlbench.adapter.cqld4.opmappers;

import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapter.cqld4.Cqld4Processors;
import io.nosqlbench.adapter.cqld4.RSProcessors;
import io.nosqlbench.adapter.cqld4.ResultSetProcessor;
import io.nosqlbench.adapter.cqld4.opdispensers.CqlD4PreparedBatchOpDispenser;
import io.nosqlbench.adapter.cqld4.opdispensers.Cqld4BatchStatementDispenser;
import io.nosqlbench.adapter.cqld4.opdispensers.Cqld4PreparedStmtDispenser;
import io.nosqlbench.adapter.cqld4.opdispensers.Cqld4SimpleCqlStmtDispenser;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.adapter.cqld4.processors.CqlFieldCaptureProcessor;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.nb.api.config.params.ParamsParser;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CqlD4CqlOpMapper implements OpMapper<Cqld4CqlOp> {
    private final CqlSession session;

    public CqlD4CqlOpMapper(CqlSession session) {
        this.session = session;
    }

    public OpDispenser<Cqld4CqlOp> apply(ParsedOp cmd) {
        ParsedTemplate stmtTpl = cmd.getStmtAsTemplate().orElseThrow(() -> new BasicError(
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


        boolean prepared = cmd.getStaticConfigOr("prepared", true);
        boolean batch = cmd.getStaticConfigOr("boolean", false);

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
