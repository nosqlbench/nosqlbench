package io.nosqlbench.virtdata.library.curves4.discrete.long_long;

import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class ZipfTest {

    @Test
    @Ignore
    public void testZipfMatrix() {

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                int elements = (int) Math.pow(10,j);
                double exponent = (double) i+1;
                System.out.println("i:" + i + " j:" + j + " elements:" + elements + " exp:" + exponent);
                long startAt = System.nanoTime();
                Zipf z = new Zipf(elements, exponent, "compute", "map");
                for (int k = 0; k < 100; k++) {
                    double frac = ((double)k/100.0d);
                    long unitvalue = (long) (frac * Long.MAX_VALUE);
                    long l = z.applyAsLong(unitvalue);
                    System.out.println("i:" + i + " j:" + j + " K:" + k + " frac:" + frac + " unit:" + unitvalue);
                }
                long endAt = System.nanoTime();
                System.out.println(((double)(endAt-startAt))/1000000000d);

            }
        }
    }

}
