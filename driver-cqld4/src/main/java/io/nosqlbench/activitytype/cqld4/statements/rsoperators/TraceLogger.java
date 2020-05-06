package io.nosqlbench.activitytype.cqld4.statements.rsoperators;

import com.datastax.oss.driver.api.core.cql.*;
import io.nosqlbench.activitytype.cqld4.api.ResultSetCycleOperator;
import io.nosqlbench.activitytype.cqld4.core.StatementModifier;
import io.nosqlbench.engine.api.util.SimpleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TraceLogger implements ResultSetCycleOperator, StatementModifier {

    private final static Logger logger = LoggerFactory.getLogger(TraceLogger.class);

    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    private final long modulo;
    private final String filename;
    private final FileWriter writer;
    private final ThreadLocal<StringBuilder> tlsb = ThreadLocal.withInitial(StringBuilder::new);

    public TraceLogger(SimpleConfig conf) {
        this(
                conf.getLong("modulo").orElse(1L),
                conf.getString("filename").orElse("tracelog")
        );
    }

    public TraceLogger(long modulo, String filename) {
        this.modulo = modulo;
        this.filename = filename;
        try {
            if (filename.equals("stdout")) {
                writer = new FileWriter(FileDescriptor.out);
            } else {
                writer = new FileWriter(filename);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int apply(ResultSet rs, Statement statement, long cycle) {
        if ((cycle%modulo)!=0) {
            return 0;
        }

        ExecutionInfo ei = rs.getExecutionInfo();
        QueryTrace qt = ei.getQueryTrace();
        StringBuilder sb = tlsb.get();
        sb.setLength(0);
        sb.append("\n---------------------------- QueryTrace Summary ---------------------------\n");
        sb.append("\n                    Coordinator: ").append(qt.getCoordinator());
        sb.append("\n                          Cycle: ").append(cycle);
        sb.append("\nServer-side query duration (us): ").append(qt.getDurationMicros());
        sb.append("\n                   Request type: ").append(qt.getRequestType());
        sb.append("\n                     Start time: ").append(qt.getStartedAt());
        sb.append("\n                     Trace UUID: ").append(qt.getTracingId());
        sb.append("\n                         Params: ").append(qt.getParameters());
        sb.append("\n--------------------------------------------------------------------------\n");
        sb.append("\n---------------------------- QueryTrace Events ---------------------------\n");
        for (TraceEvent event : qt.getEvents()) {
            sb.append("\n               Date: ").append(sdf.format(new Date(event.getTimestamp())));
            sb.append("\n             Source: ").append(event.getSource());
            sb.append("\nSourceElapsedMicros: ").append(event.getSourceElapsedMicros());
            sb.append("\n             Thread: ").append(event.getThreadName());
            sb.append("\n           Activity: ").append(event.getActivity()).append("\n");
        }
        sb.append("\n--------------------------------------------------------------------------\n");

        try {
            writer.append(sb.toString());
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }

    @Override
    public Statement modify(Statement statement, long cycle) {
        if ((cycle%modulo)==0) {
            return statement.setTracing(true);
        }
        return statement;
    }
}
