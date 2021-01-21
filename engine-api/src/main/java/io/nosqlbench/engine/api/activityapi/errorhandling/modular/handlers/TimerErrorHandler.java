package io.nosqlbench.engine.api.activityapi.errorhandling.modular.handlers;

import io.nosqlbench.engine.api.activityapi.errorhandling.ErrorMetrics;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorHandler;
import io.nosqlbench.engine.api.metrics.ExceptionTimerMetrics;
import io.nosqlbench.nb.annotations.Service;

import java.util.function.Supplier;

@Service(value = ErrorHandler.class, selector = "timer")
public class TimerErrorHandler implements ErrorHandler, ErrorMetrics.Aware {


    private ExceptionTimerMetrics exceptionTimerMetrics;

    @Override
    public ErrorDetail handleError(Throwable t, long cycle, long durationInNanos, ErrorDetail detail) {
        exceptionTimerMetrics.update(t, durationInNanos);
        return detail;
    }

    @Override
    public void setErrorMetricsSupplier(Supplier<ErrorMetrics> supplier) {
        this.exceptionTimerMetrics = supplier.get().getExceptionTimerMetrics();
    }
}
