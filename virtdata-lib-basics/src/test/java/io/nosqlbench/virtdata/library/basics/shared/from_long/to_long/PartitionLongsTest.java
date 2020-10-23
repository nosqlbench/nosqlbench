package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class PartitionLongsTest {

    @Test
    void testInvalidInitializerValues() {
        Assertions.assertThrows(RuntimeException.class, () -> new PartitionLongs(-3));
        Assertions.assertThrows(RuntimeException.class, () -> new PartitionLongs(0));
    }

    @Test
    public void testValueCardinality() {
        PartitionLongs f = new PartitionLongs(15);
        Set<Long> values = new HashSet<Long>();
        for (int i = 0; i < 100; i++) {
            values.add(f.applyAsLong(i));
        }
        Assertions.assertEquals(15, values.size());
    }
}
