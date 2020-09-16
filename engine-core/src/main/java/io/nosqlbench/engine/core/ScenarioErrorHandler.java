package io.nosqlbench.engine.core;

import io.nosqlbench.nb.api.errors.BasicError;
import org.graalvm.polyglot.PolyglotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 *
 * That is all. When this error handler is invoked, it is a foregone conclusion that the scenario
 * is not able to continue, else the error would have been trapped and handled internal to a lower-level
 * class. It is the calling exception handler's responsibility to finally shut down the scenario
 * cleanly and return appropriately. Thus, <em>You should not throw errors from this class. You should only
 * unwrap and explain errors, sending contents to the logfile as appropriate.</em>
 *
 */
public class ScenarioErrorHandler {

    private final static Logger logger = LoggerFactory.getLogger(ScenarioErrorHandler.class);

    public static String handle(Throwable t, boolean wantsStackTraces) {
        if (t instanceof ScriptException) {
            return handleScriptException((ScriptException) t, wantsStackTraces);
        } else if (t instanceof BasicError) {
            return handleBasicError((BasicError) t, wantsStackTraces);
        } else if (t instanceof Exception){
            return handleInternalError((Exception) t, wantsStackTraces);
        } else {
          throw new RuntimeException("Error in exception handler", t);
        }
    }

    private static String handleInternalError(Exception e, boolean wantsStackTraces) {
        String prefix = "internal error: ";
        if (e.getCause()!=null && !e.getCause().getClass().getCanonicalName().contains("io.nosqlbench")) {
            prefix = "Error from driver or included library: ";
        }

        if (wantsStackTraces) {
            logger.error(prefix + e.getMessage(),e);
        } else {
            logger.error(e.getMessage());
            logger.error("for the full stack trace, run with --show-stacktraces");
        }
        return e.getMessage();
    }

    private static String handleScriptException(ScriptException e, boolean wantsStackTraces) {
        Throwable cause = e.getCause();
        if (cause instanceof PolyglotException) {
            Throwable hostException = ((PolyglotException) cause).asHostException();
            if (hostException instanceof BasicError) {
                handleBasicError((BasicError)hostException, wantsStackTraces);
            } else {
                handle(hostException, wantsStackTraces);
            }
        } else {
            if (wantsStackTraces) {
                logger.error("Unknown script exception:",e);
            } else {
                logger.error(e.getMessage());
                logger.error("for the full stack trace, run with --show-stacktraces");
            }
        }
        return e.getMessage();
    }

    private static String handleBasicError(BasicError e, boolean wantsStackTraces) {
        if (wantsStackTraces) {
            logger.error(e.getMessage(),e);
        } else {
            logger.error(e.getMessage());
            logger.error("for the full stack trace, run with --show-stacktraces");
        }
        return e.getMessage();
    }

}
