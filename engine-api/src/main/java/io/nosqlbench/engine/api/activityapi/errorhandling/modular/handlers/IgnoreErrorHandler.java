package io.nosqlbench.engine.api.activityapi.errorhandling.modular.handlers;

import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorHandler;
import io.nosqlbench.nb.annotations.Service;

@Service(value = ErrorHandler.class, selector = "ignore")
public class IgnoreErrorHandler implements ErrorHandler {

    @Override
    public ErrorDetail handleError(String name, Throwable t, long cycle, long durationInNanos, ErrorDetail detail) {
        return detail;
    }

}
