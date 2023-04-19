package io.nosqlbench.api.engine.metrics.micro;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public abstract class MicroObservable implements Timer,
        TimerAttachment, DeltaSnapshotter, HdrDeltaHistogramAttachment {

    enum Type {
        COUNTER, GAUGE, LONG_TASK_TIMER, TIMER, DISTRIBUTION_SUMMARY, OTHER;
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

        private final String tagValueRepresentation;

        Statistic(String tagValueRepresentation) {
            this.tagValueRepresentation = tagValueRepresentation;
        }

        public String getTagValueRepresentation() {
            return tagValueRepresentation;
        }

    }

    /**
     * @param name         required name
     * @param type         required type of Observable.Type
     * @param tags         list of optional string tag as an even number of arguments representing key/value pairs
     * @param baseUnit     optional base unit
     * @param description  optional description of observable
     * @param measurements required measurements representing instantaneous value of this Observable
     * @param registry     required MeterRegistry
     * @return Meter after adding the meter to a single registry, or return an existing meter in that registry.
     */
    public Meter init(String name,
                      Type type,
                      String[] tags,
                      String baseUnit,
                      String description,
                      Iterable<Measurement> measurements,
                      MeterRegistry registry) {

        if (name == null || type == null || measurements == null || registry == null) {
            throw new IllegalArgumentException("Required name, type, measurements, and registry.");
        }

        final Meter.Builder meterBuilder = Meter.builder(name, Meter.Type.valueOf(type.name()), measurements);
        meterBuilder.tags(tags);
        meterBuilder.description(description);
        meterBuilder.baseUnit(baseUnit);
        return meterBuilder.register(registry);
    }


}
