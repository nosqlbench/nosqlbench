package io.nosqlbench.engine.api.activityapi.errorhandling;

import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.metrics.ExceptionCountMetrics;
import io.nosqlbench.engine.api.metrics.ExceptionHistoMetrics;
import io.nosqlbench.engine.api.metrics.ExceptionMeterMetrics;
import io.nosqlbench.engine.api.metrics.ExceptionTimerMetrics;

import java.util.function.Supplier;

public class ErrorMetrics {

    private final ActivityDef activityDef;
    private ExceptionCountMetrics exceptionCountMetrics;
    private ExceptionHistoMetrics exceptionHistoMetrics;
    private ExceptionMeterMetrics exceptionMeterMetrics;
    private ExceptionTimerMetrics exceptionTimerMetrics;

    public ErrorMetrics(ActivityDef activityDef) {
        this.activityDef = activityDef;
    }

    public synchronized ExceptionCountMetrics getExceptionCountMetrics() {
        if (exceptionCountMetrics == null) {
            exceptionCountMetrics = new ExceptionCountMetrics(activityDef);
        }
        return exceptionCountMetrics;
    }

    public synchronized ExceptionHistoMetrics getExceptionHistoMetrics() {
        if (exceptionHistoMetrics == null) {
            exceptionHistoMetrics = new ExceptionHistoMetrics(activityDef);
        }
        return exceptionHistoMetrics;
    }

    public synchronized ExceptionMeterMetrics getExceptionMeterMetrics() {
        if (exceptionMeterMetrics == null) {
            exceptionMeterMetrics = new ExceptionMeterMetrics(activityDef);
        }
        return exceptionMeterMetrics;
    }

    public synchronized ExceptionTimerMetrics getExceptionTimerMetrics() {
        if (exceptionTimerMetrics == null) {
            exceptionTimerMetrics = new ExceptionTimerMetrics(activityDef);
        }
        return exceptionTimerMetrics;
    }

    public interface Aware {
        void setErrorMetricsSupplier(Supplier<ErrorMetrics> supplier);
    }

}
