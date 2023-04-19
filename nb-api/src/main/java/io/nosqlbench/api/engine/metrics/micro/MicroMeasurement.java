package io.nosqlbench.api.engine.metrics.micro;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class MicroMeasurement {

    private final DoubleSupplier doubleSupplier;

    private final Statistic statistic;

    public MicroMeasurement(DoubleSupplier valueFunction, Statistic statistic) {
        this.doubleSupplier = valueFunction;
        this.statistic = statistic;
    }

    public MicroMeasurement(Supplier<Double> valueFunction, Statistic statistic) {
        this.doubleSupplier = valueFunction::get;
        this.statistic = statistic;
    }

    public double getValue() {
        return doubleSupplier.getAsDouble();
    }

    public Statistic getStatistic() {
        return statistic;
    }

    @Override
    public String toString() {
        return "Measurement{" + "statistic='" + statistic + '\'' + ", value=" + getValue() + '}';
    }

    public enum Statistic {

        /**
         * The sum of the amounts recorded.
         */
        TOTAL("total"),

        /**
         * The sum of the times recorded. Reported in the monitoring system's base unit of
         * time
         */
        TOTAL_TIME("total"),

        /**
         * Rate per second for calls.
         */
        COUNT("count"),

        /**
         * The maximum amount recorded. When this represents a time, it is reported in the
         * monitoring system's base unit of time.
         */
        MAX("max"),

        /**
         * Instantaneous value, such as those reported by gauges.
         */
        VALUE("value"),

        /**
         * Undetermined.
         */
        UNKNOWN("unknown"),

        /**
         * Number of currently active tasks for a long task timer.
         */
        ACTIVE_TASKS("active"),

        /**
         * Duration of a running task in a long task timer. Always reported in the monitoring
         * system's base unit of time.
         */
        DURATION("duration");

        private final String value;

        Statistic(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

}
