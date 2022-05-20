package io.nosqlbench.activitytype.cql.statements.rsoperators;

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


import com.datastax.driver.core.ExecutionInfo;
import com.datastax.driver.core.QueryTrace;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Statement;
import io.nosqlbench.activitytype.cql.api.ResultSetCycleOperator;
import io.nosqlbench.activitytype.cql.statements.modifiers.StatementModifier;
import io.nosqlbench.engine.api.util.SimpleConfig;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TraceLogger implements ResultSetCycleOperator, StatementModifier {

    private final static Logger logger = LogManager.getLogger(TraceLogger.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
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
        sb.append("\n                     Trace UUID: ").append(qt.getTraceId());
        sb.append("\n                         Params: ").append(qt.getParameters());
        sb.append("\n--------------------------------------------------------------------------\n");
        sb.append("\n---------------------------- QueryTrace Events ---------------------------\n");
        for (QueryTrace.Event event : qt.getEvents()) {
            sb.append("\n               Date: ").append(sdf.format(new Date(event.getTimestamp())));
            sb.append("\n             Source: ").append(event.getSource());
            sb.append("\nSourceElapsedMicros: ").append(event.getSourceElapsedMicros());
            sb.append("\n             Thread: ").append(event.getThreadName());
            sb.append("\n        Description: ").append(event.getDescription()).append("\n");
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
            statement.enableTracing();
        }
        return statement;
    }
}
