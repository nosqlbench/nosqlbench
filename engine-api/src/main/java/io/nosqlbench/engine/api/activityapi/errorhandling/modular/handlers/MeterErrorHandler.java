package io.nosqlbench.engine.api.activityapi.errorhandling.modular.handlers;

import io.nosqlbench.engine.api.activityapi.errorhandling.ErrorMetrics;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorHandler;
import io.nosqlbench.engine.api.metrics.ExceptionMeterMetrics;
import io.nosqlbench.nb.annotations.Service;

import java.util.function.Supplier;

@Service(value = ErrorHandler.class, selector = "meter")
public class MeterErrorHandler implements ErrorHandler, ErrorMetrics.Aware {
    private ExceptionMeterMetrics exceptionMeterMetrics;

    @Override
    public ErrorDetail handleError(String name, Throwable t, long cycle, long durationInNanos, ErrorDetail detail) {
        exceptionMeterMetrics.mark(name);
        return detail;
    }

    @Override
    public void setErrorMetricsSupplier(Supplier<ErrorMetrics> supplier) {
        this.exceptionMeterMetrics = supplier.get().getExceptionMeterMetrics();
    }
}
