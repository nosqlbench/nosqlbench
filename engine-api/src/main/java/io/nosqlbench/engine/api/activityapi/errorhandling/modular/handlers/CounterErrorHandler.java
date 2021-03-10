package io.nosqlbench.engine.api.activityapi.errorhandling.modular.handlers;

import io.nosqlbench.engine.api.activityapi.errorhandling.ErrorMetrics;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorHandler;
import io.nosqlbench.engine.api.metrics.ExceptionCountMetrics;
import io.nosqlbench.nb.annotations.Service;

import java.util.function.Supplier;

@Service(value = ErrorHandler.class, selector = "counter")
public class CounterErrorHandler implements ErrorHandler, ErrorMetrics.Aware {

    private ExceptionCountMetrics exceptionCountMetrics;

    @Override
    public ErrorDetail handleError(Throwable t, long cycle, long durationInNanos, ErrorDetail detail) {
        exceptionCountMetrics.count(t);
        return detail;
    }

    @Override
    public void setErrorMetricsSupplier(Supplier<ErrorMetrics> supplier) {
        this.exceptionCountMetrics = supplier.get().getExceptionCountMetrics();
    }
}
