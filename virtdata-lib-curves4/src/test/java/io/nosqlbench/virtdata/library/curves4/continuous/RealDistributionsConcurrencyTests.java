package io.nosqlbench.virtdata.library.curves4.continuous;

import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

public class RealDistributionsConcurrencyTests {

    @Test
    public void testConcurrentBinomialHashValues() {
        testConcurrentRealHashDistValues(
                "normal(10.0,2.0)/100 threads/1000 iterations",
                100,
                1000,
                "normal(10.0,2.0)");
    }

    private void testConcurrentRealHashDistValues(
            String description,
            int threads,
            int iterations,
            String mapperSpec) {

        DataMapper<Double> mapper = VirtData.getMapper(mapperSpec, double.class);
        double[] values = new double[iterations];
        for (int index = 0; index < iterations; index++) {
            values[index] = mapper.get(index);
        }

        ExecutorService pool = Executors.newFixedThreadPool(threads);

        List<Future<double[]>> futures = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            futures.add(pool.submit(new RealDistributionCallable(t, iterations, mapperSpec, pool)));
        }
        try {
            Thread.sleep(1000);
            synchronized (pool) {
                pool.notifyAll();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        List<double[]> results = new ArrayList<>();
//        long[][] results = new long[threads][iterations];

        for (int i = 0; i < futures.size(); i++) {
            try {
                results.add(futures.get(i).get());
//                System.out.println(description + ": got results for thread " + i);
//                System.out.flush();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        pool.shutdown();

        for (int vthread = 0; vthread < threads; vthread++) {
            assertThat(results.get(vthread)).isEqualTo(values);
            System.out.println(description + ": verified values for thread " + vthread);
        }


    }

    private static class RealDistributionCallable implements Callable<double[]> {

        private final Object signal;
        private final int slot;
        private final String mapperSpec;
        private final int size;

        public RealDistributionCallable(int slot, int size, String mapperSpec, Object signal) {
            this.slot = slot;
            this.size = size;
            this.mapperSpec = mapperSpec;
            this.signal = signal;
        }

        @Override
        public double[] call() throws Exception {
            double[] output = new double[size];
            DataMapper<Double> mapper = VirtData.getMapper(mapperSpec, double.class);
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
