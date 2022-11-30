/*
 * Copyright (c) 2022 nosqlbench
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

import java.util.Optional;

/**
 * Provide a result type back to a caller, including the start and end times,
 * any exception that occurred, and any content written to stdout or stderr equivalent
 * IO streams. This is an <EM>execution result</EM>.
 *
 */
public class ExecResult {
    protected final static Logger logger = LogManager.getLogger(ExecMetricsResult.class);
    protected final long startedAt;
    protected final long endedAt;
    protected final Exception exception;
    protected final String iolog;

    public ExecResult(long startedAt, long endedAt, String iolog, Exception e) {
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.exception = e;
        this.iolog = ((iolog != null) ? iolog + "\n\n" : "") + (e != null ? e.getMessage() : "");
        logger.debug("populating "+(e==null? "NORMAL" : "ERROR")+" scenario result");
        if (logger.isDebugEnabled()) {
            StackTraceElement[] st = Thread.currentThread().getStackTrace();
            for (int i = 0; i < st.length; i++) {
                logger.debug(":AT " + st[i].getFileName()+":"+st[i].getLineNumber()+":"+st[i].getMethodName());
                if (i>10) break;
            }
        }

    }

    public void reportElapsedMillisToLog() {
        logger.info("-- SCENARIO TOOK " + getElapsedMillis() + "ms --");
    }

    public String getIOLog() {
        return this.iolog;
    }

    public long getElapsedMillis() {
        return endedAt - startedAt;
    }

    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }
}
