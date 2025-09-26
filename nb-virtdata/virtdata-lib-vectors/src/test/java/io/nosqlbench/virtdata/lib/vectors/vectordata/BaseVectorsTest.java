package io.nosqlbench.virtdata.lib.vectors.vectordata;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class BaseVectorsTest {

    @Test
    @Disabled
    public void testExampleDataset() {
        BaseVectors bv1 = new BaseVectors("example1");
        float[] v23 = bv1.apply(23L);
        System.out.println("v23:" + Arrays.toString(v23));
    }
}
