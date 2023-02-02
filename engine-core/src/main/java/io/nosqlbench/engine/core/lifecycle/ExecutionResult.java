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

/**
 * Provide a result type back to a caller, including the start and end times,
 * any exception that occurred, and any content written to stdout or stderr equivalent
 * IO streams. This is an <EM>execution result</EM>.
 *
 */
public class ExecutionResult {
    protected final static Logger logger = LogManager.getLogger(ExecutionResult.class);
    protected final long startedAt;
    protected final long endedAt;
    protected final Exception exception;
    protected final String iolog;

    public ExecutionResult(long startedAt, long endedAt, String iolog, Exception error) {
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.exception = error;
        this.iolog = ((iolog != null) ? iolog + "\n\n" : "") + exception;
        logger.debug("populating "+(error==null ? "NORMAL" : "ERROR")+" scenario result");
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
        return this.iolog;
    }

    public long getElapsedMillis() {
        return endedAt - startedAt;
    }

    public Exception getException() {
        return exception;
    }
}
