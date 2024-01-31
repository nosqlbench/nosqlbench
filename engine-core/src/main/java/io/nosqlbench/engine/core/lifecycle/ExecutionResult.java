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

package io.nosqlbench.engine.core.lifecycle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Provide a result type back to a caller, including the start and end times,
 * any exception that occurred, and any content written to stdout or stderr equivalent
 * IO streams. This is an <EM>execution result</EM>.
 *
 */
public class ExecutionResult {
    protected final static Logger logger = LogManager.getLogger(ExecutionResult.class);
    protected final Status status;

    public void printSummary(PrintStream out) {
        out.println(this);
    }

    public enum Status {
        OK(0),
        WARNING(1),
        ERROR(2);
        public final int code;

        Status(int code) {
            this.code = code;
        }
    }
    protected final long startedAt;
    protected final long endedAt;
    protected final Exception exception;
    protected final String iolog;

    public ExecutionResult(long startedAt, long endedAt, String iolog, Exception error) {
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        List<String> elements = new ArrayList<>();
        if (iolog!=null && iolog.isEmpty()) {
            elements.add(iolog);
        }
        if (error!=null) {
            elements.add("ERROR:" + error);
        }
        this.exception = error;
        this.iolog = String.join("\n",elements);
        this.status = (error==null) ? Status.OK : Status.ERROR;
        logger.debug("populating "+status+" scenario result");

//        if (logger.isTraceEnabled()) {
//            StackTraceElement[] st = Thread.currentThread().getStackTrace();
//            for (int i = 0; i < st.length; i++) {
//                logger.debug(":AT " + st[i].getFileName()+":"+st[i].getLineNumber()+":"+st[i].getMethodName());
//                if (i>10) break;
//            }
//        }

    }

    public void reportElapsedMillisToLog() {
        logger.info(() -> String.format("-- SCENARIO TOOK %.3fS --",(getElapsedMillis()/1000.0f)));
    }

    public String getIOLog() {
        return this.iolog==null? "" : iolog;
    }

    public long getElapsedMillis() {
        return endedAt - startedAt;
    }

    public Exception getException() {
        return exception;
    }
    public void rethrow() {
        if (exception!=null) {
            if (exception instanceof RuntimeException rte) {
                throw rte;
            } else {
                throw new RuntimeException("Exception in result:" + exception,exception);
            }
        }
    }

    public Status getStatus() {
        return this.status;
    }

    @Override
    public String toString() {
        return "ExecutionResult{" +
            "status=" + status +
            ", startedAt=" + startedAt +
            ", endedAt=" + endedAt +
            ", total=" + String.format("%.2fS",(((double)getElapsedMillis())/1000.0)) +
            ", exception=" + exception +
            ", iolog='" + iolog + '\'' +
            '}';
    }
}
