package io.nosqlbench.api.engine.metrics.micro;

import io.micrometer.core.instrument.Timer;

public interface TimerAttachment {
    Timer attachTimer(Timer timer);
}

