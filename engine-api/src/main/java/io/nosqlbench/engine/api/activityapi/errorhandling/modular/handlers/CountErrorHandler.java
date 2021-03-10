package io.nosqlbench.engine.api.activityapi.errorhandling.modular.handlers;

import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorHandler;
import io.nosqlbench.nb.annotations.Service;

/**
 * This is here to allow the classic name 'count' to work although the
 * modern error handler scheme uses canonical metric type names.
 */
@Service(value = ErrorHandler.class, selector = "count")
public class CountErrorHandler extends CounterErrorHandler {
}
