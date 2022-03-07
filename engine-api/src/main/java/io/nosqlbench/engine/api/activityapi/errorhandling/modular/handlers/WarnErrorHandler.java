package io.nosqlbench.engine.api.activityapi.errorhandling.modular.handlers;

import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorHandler;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service(value= ErrorHandler.class, selector="warn")
public class WarnErrorHandler implements ErrorHandler {
    private final static Logger logger = LogManager.getLogger("ERRORS");

    @Override
    public ErrorDetail handleError(String name, Throwable t, long cycle, long durationInNanos, ErrorDetail detail) {
        logger.warn("error with cycle " + cycle + " errmsg: " + t.getMessage());
        return detail;
    }

}
