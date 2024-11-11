/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.engine.core.lifecycle.process;

import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.nb.api.advisor.NBAdvisorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.PolyglotException;

import javax.script.ScriptException;

/**
 * This class is meant to consolidate the error handling logic for the varios types of errors
 * that may bubble up from the layers within NoSQLBench. As a layered system, some of the included
 * libraries tend to layer the exceptions beyond a point of recognizability. The logic in this
 * class should do the following:
 *
 * <ol>
 *     <li>Report an error in the most intelligible way to the user.</li>
 * </ol>
 * <p>
 * That is all. When this error handler is invoked, it is a foregone conclusion that the scenario
 * is not able to continue, else the error would have been trapped and handled internal to a lower-level
 * class. It is the calling exception handler's responsibility to finally shut down the scenario
 * cleanly and return appropriately. Thus, <em>You should not throw errors from this class. You should only
 * unwrap and explain errors, sending contents to the logfile as appropriate.</em>
 */
public class NBCLIErrorHandler {

    private final static Logger logger = LogManager.getLogger("ERRORHANDLER");

    public static int handle(Throwable t, boolean wantsStackTraces, String version) {

        if (wantsStackTraces) {
            StackTraceElement[] st = Thread.currentThread().getStackTrace();

            for (int i = 0; i < 10; i++) {
                if (st.length > i) {
                    String className = st[i].getClassName();
                    String fileName = st[i].getFileName();
                    int lineNumber = st[i].getLineNumber();
                    logger.trace("st[" + i + "]:" + className + "," + fileName + ":" + lineNumber);
                }
            }
        }
        if (t instanceof ScriptException) {
            logger.trace("Handling script exception: " + t);
            return handleScriptException((ScriptException) t, wantsStackTraces, version);
        } else if (t instanceof BasicError) {
            logger.trace("Handling basic error: " + t);
            return handleBasicError((BasicError) t, wantsStackTraces, version);
        } else if (t instanceof NBAdvisorException) {
            logger.trace("Handle processing early exit: " + t);
            return handleNBAdvisorException((NBAdvisorException) t, wantsStackTraces, version);
        } else if (t instanceof Exception) {
            logger.trace("Handling general exception: " + t);
            return handleInternalError((Exception) t, wantsStackTraces, version);
        } else {
            logger.error("Unknown type for error handler(" + version + "):" + t);
            throw new RuntimeException("Error in exception handler", t);
        }
    }

    private static int handleInternalError(Exception e, boolean wantsStackTraces, String version) {
        String prefix = "internal error(" + version + "):";
        if (e.getCause() != null && !e.getCause().getClass().getCanonicalName().contains("io.nosqlbench")) {
            prefix = "Error from driver or included library(" + version + "):";
        }

        if (wantsStackTraces) {
            logger.error(prefix + e.getMessage(), e);
            if (e.getCause() != null) {
                logger.error("cause (see stack trace for details):" + e.getCause().getMessage());
            }
        } else {
            logger.error(e.getMessage());
            logger.error("for the full stack trace, run with --show-stacktraces");
        }
        return 2;
    }

    private static int handleScriptException(ScriptException e, boolean wantsStackTraces, String version) {
        Throwable cause = e.getCause();
        if (cause instanceof PolyglotException) {
            Throwable hostException = ((PolyglotException) cause).asHostException();
            if (hostException instanceof BasicError) {
                handleBasicError((BasicError) hostException, wantsStackTraces, version);
            } else {
                handle(hostException, wantsStackTraces, version);
            }
        } else {
            if (wantsStackTraces) {
                logger.error("Unknown script exception(" + version + "):", e);
            } else {
                logger.error(e.getMessage());
                logger.error("for the full stack trace, run with --show-stacktraces");
            }
        }
        return 2;
    }

    private static int handleBasicError(BasicError e, boolean wantsStackTraces, String version) {
        if (wantsStackTraces) {
            logger.error(e.getMessage(), e);
        } else {
            logger.error(e.getMessage());
            logger.error("for the full stack trace, run with --show-stacktraces");
        }
        return 2;
    }

    private static int handleNBAdvisorException(NBAdvisorException e, boolean wantsStackTraces, String version) {
	logger.info(e.toString());
        return e.getExitCode();
    }

}
