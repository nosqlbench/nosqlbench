package io.nosqlbench.engine.api.activityapi.errorhandling.modular.handlers;

import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorHandler;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service(value = ErrorHandler.class, selector = "stop")
public class StopErrorHandler implements ErrorHandler {

    private final static Logger logger = LogManager.getLogger(StopErrorHandler.class);

    @Override
    public ErrorDetail handleError(Throwable t, long cycle, long durationInNanos, ErrorDetail detail) {
        String durationSummary = String.format("%.3fS", ((double) durationInNanos / 1000000000.0));
        throw new RuntimeException(
            "An error was rethrown in order to stop the activity in cycle:" + cycle + ", duration:" + durationSummary + " msg:" +
                t.getMessage());
    }
}
