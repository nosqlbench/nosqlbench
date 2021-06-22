package io.nosqlbench.virtdata.library.basics.core.stathelpers;

import org.junit.jupiter.api.Test;

public class DiscreteProbabilityBufferTest {

    @Test
    public void testReplay() {
        DiscreteProbabilityBuffer dp = new DiscreteProbabilityBuffer(10);
        dp.add(1,2.0D);
        dp.add(2,2.0D);
        dp.add(3,4.0D);
        dp.add(4,8.0D);
        dp.add(5,16.0D);
        dp.add(6,32.0D);
        dp.add(7,64.0D);
        dp.add(8,128.0D);
        dp.add(9,256.0D);
        dp.add(10,512.0D);
        dp.normalize();

        for (DiscreteProbabilityBuffer.Entry entry : dp) {
            System.out.println("entry: " + entry.getEventId() + ":" + entry.getProbability());
        }
        System.out.println("cumuProb:" + dp.getCumulativeProbability());
    }

}