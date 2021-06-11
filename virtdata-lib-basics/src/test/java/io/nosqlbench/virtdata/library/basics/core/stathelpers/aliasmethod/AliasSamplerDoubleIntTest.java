package io.nosqlbench.virtdata.library.basics.core.stathelpers.aliasmethod;

import io.nosqlbench.virtdata.library.basics.core.stathelpers.AliasSamplerDoubleInt;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.EvProbD;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AliasSamplerDoubleIntTest {

    @Test
    public void testAliasSamplerBinaryFractions() {
        List<EvProbD> events = new ArrayList<>();
        events.add(new EvProbD(1,1.0D));
        events.add(new EvProbD(2,1.0D));
        events.add(new EvProbD(3,2.0D));
        events.add(new EvProbD(4,4.0D));
        events.add(new EvProbD(5,8.0D));
        events.add(new EvProbD(6,16.0D));
        events.add(new EvProbD(7,32.0D));
        events.add(new EvProbD(8,64.0D));

        AliasSamplerDoubleInt as = new AliasSamplerDoubleInt(events);
        int[] stats = new int[9];
        for (int i = 0; i < 10000; i++) {
            double v = (double)i / 10000D;
            int idx = as.applyAsInt(v);
            stats[idx]++;
        }
        System.out.println(Arrays.toString(stats));
        assertThat(stats).containsExactly(0,79,79,157,313,626,1250,2499,4997);

    }

    @Test
    public void testAliasSamplerSimple() {
        List<EvProbD> events = new ArrayList<>();
        events.add(new EvProbD(1,1D));
        events.add(new EvProbD(2,2D));
        events.add(new EvProbD(3,3D));

        AliasSamplerDoubleInt as = new AliasSamplerDoubleInt(events);

        int[] stats = new int[4];
        for (int i = 0; i < 10000; i++) {
            double v = (double)i / 10000D;
            int idx = as.applyAsInt(v);
            stats[idx]++;
        }
        System.out.println(Arrays.toString(stats));
        assertThat(stats).containsExactly(0,1667,3334,4999);
    }


    // Single threaded performance: 100000000 ops in 1366334133 nanos for 73188539.746449 ops/s
    // yes, that is >70M discrete probability samples per second, but hey, it's only 3 discrete probabilities in this test
    @Test
    @Disabled
    public void testAliasMicroBenchSmallMany() {
        List<EvProbD> events = new ArrayList<>();
        events.add(new EvProbD(1,1D));
        events.add(new EvProbD(2,2D));
        events.add(new EvProbD(3,3D));

        AliasSamplerDoubleInt as = new AliasSamplerDoubleInt(events);

        long count=1_000_000_00;
        long startAt = System.nanoTime();
        for (int i = 0; i < count; i++) {
            double v = (double)i / count;
            int idx = as.applyAsInt(v);
        }
        long endAt = System.nanoTime();
        long nanos = endAt - startAt;
        double oprate = ((double) count / (double) nanos) * 1_000_000_000D;
        System.out.format("Single threaded performance: %d ops in %d nanos for %f ops/s\n", count, nanos, oprate);
    }

    // Single threaded performance: 100000000 ops in 1346200937 nanos for 74283115.730739 ops/s
    // yes, that is >70M discrete probability samples per second, but hey, it's only 1M discrete probabilities in this test,
    @Test
    public void testAliasMicroBenchLargeMany() {
        List<EvProbD> events = new ArrayList<>();
        int evt_count=1_000_000;
        for (int i = 0; i < evt_count; i++) {
            double val = (double)i/(double)evt_count;
            events.add(new EvProbD(i,val));
        }
        AliasSamplerDoubleInt as = new AliasSamplerDoubleInt(events);

        long count=100_000_000;
        long startAt = System.nanoTime();
        for (int i = 0; i < count; i++) {
            double v = (double)i / count;
            int idx = as.applyAsInt(v);
        }
        long endAt = System.nanoTime();
        long nanos = endAt - startAt;
        double oprate = ((double) count / (double) nanos) * 1_000_000_000D;
        System.out.format("Single threaded performance: %d ops in %d nanos for %f ops/s\n", count, nanos, oprate);
    }


}