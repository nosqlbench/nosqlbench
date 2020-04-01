package io.nosqlbench.virtdata.library.curves4.discrete;

import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import org.apache.commons.statistics.distribution.BinomialDistribution;
import org.assertj.core.data.Offset;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegerDistributionsConcurrencyTest {

    @Test
    public void testBinomialICDR() {
        Offset<Double> offset = Offset.offset(0.00001d);
        BinomialDistribution distribution = new BinomialDistribution(8, 0.5);
        assertThat(distribution.probability(0)).isCloseTo(0.00390d, offset);
        assertThat(distribution.probability(1)).isCloseTo(0.03125d, offset);
        assertThat(distribution.probability(2)).isCloseTo(0.10937d, offset);
        assertThat(distribution.probability(3)).isCloseTo(0.21875d, offset);
        assertThat(distribution.probability(4)).isCloseTo(0.27343d, offset);
        assertThat(distribution.probability(5)).isCloseTo(0.21875d, offset);
        assertThat(distribution.probability(6)).isCloseTo(0.10937d, offset);
        assertThat(distribution.probability(7)).isCloseTo(0.03125d, offset);
        assertThat(distribution.probability(8)).isCloseTo(0.00390d, offset);
    }

    @Test
    public void testBinomialCurvePoints() {
        DataMapper<Long> mapper = VirtData.getMapper("Binomial(8,0.5,'map') -> long", long.class);

        long half = Long.MAX_VALUE / 2;
        long expected = mapper.get(half);
        assertThat(expected).isEqualTo(4);
        expected = mapper.get(1);
        assertThat(expected).isEqualTo(0);

        // threshold test against CDF
        expected = mapper.get((long) (0.03515d * (double) Long.MAX_VALUE));
        assertThat(expected).isEqualTo(1);
        expected = mapper.get((long) (0.03600d * (double) Long.MAX_VALUE));
        assertThat(expected).isEqualTo(2);
    }

    @Test
    public void testConcurrentBinomialHashValues() {
        testConcurrentIntegerHashDistValues(
                "Binomial(8,0.5)/100 threads/1000 iterations",
                100,
                1000,
                "Binomial(8,0.5) -> long");
    }

    private void testConcurrentIntegerHashDistValues(
            String description,
            int threads,
            int iterations,
            String mapperSpec) {


        DataMapper<Long> mapper = VirtData.getMapper(mapperSpec, long.class);
        long[] values = new long[iterations];
        for (int index = 0; index < iterations; index++) {
            values[index] = mapper.get(index);
        }

        ExecutorService pool = Executors.newFixedThreadPool(threads);

        List<Future<long[]>> futures = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            futures.add(pool.submit(new IntegerDistributionCallable(t, iterations, mapperSpec, pool)));
        }
        try {
            Thread.sleep(1000);
            synchronized (pool) {
                pool.notifyAll();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        long[][] results = new long[threads][iterations];

        for (int i = 0; i < futures.size(); i++) {
            try {
                results[i] = futures.get(i).get();
                System.out.println(description + ": got results for thread " + i);
                System.out.flush();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        pool.shutdown();

        for (int vthread = 0; vthread < threads; vthread++) {
            long[] threadResults = results[vthread];
            for (int i = 0; i <values.length ; i++) {
                if (threadResults[i] != values[i]) {
                    System.out.println("not equal in thread="+ vthread + ", i=" + i
                            +", " + threadResults[i] + "!=" + values[i]);
                    for (int ithread = 0; ithread < threads; ithread++) {
                        System.out.print(results[ithread][i] + ",");
                    }
                    System.out.println();
                }
            }
            boolean equal = Arrays.equals(results[vthread],values);
            if (!equal) {
                System.out.println("not equal!");
            }
            assertThat(results[vthread]).isEqualTo(values);
            System.out.println(description + ": verified values for thread " + vthread);
        }


    }

    private static class IntegerDistributionCallable implements Callable<long[]> {

        private final Object signal;
        private final int slot;
        private final String mapperSpec;
        private int size;

        public IntegerDistributionCallable(int slot, int size, String mapperSpec, Object signal) {
            this.slot = slot;
            this.size = size;
            this.mapperSpec = mapperSpec;
            this.signal = signal;
        }

        @Override
        public long[] call() throws Exception {
            long[] output = new long[size];
            DataMapper<Long> mapper = VirtData.getMapper(mapperSpec, long.class);
//            System.out.println("resolved:" + mapper);
//            System.out.flush();

            synchronized (signal) {
                signal.wait(10000);
            }

            for (int i = 0; i < output.length; i++) {
                output[i] = mapper.get(i);
//                if ((i % 100) == 0) {
//                    System.out.println("wrote t:" + slot + ", iter:" + i + ", val:" + output[i]);
//                }
            }
            return output;
        }
    }

}
