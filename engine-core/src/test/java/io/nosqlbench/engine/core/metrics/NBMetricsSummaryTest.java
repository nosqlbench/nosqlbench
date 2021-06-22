package io.nosqlbench.engine.core.metrics;

import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.metrics.DeltaHdrHistogramReservoir;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class NBMetricsSummaryTest {

    @Test
    public void testFormat() {
        StringBuilder sb = new StringBuilder();
        Timer timer = new Timer(new DeltaHdrHistogramReservoir("test", 4));

        for (int i = 0; i < 100000; i++) {
            timer.update((i % 1000) + 1, TimeUnit.MILLISECONDS);
        }

        NBMetricsSummary.summarize(sb, "test", timer);

        System.out.println(sb);
    }

}
