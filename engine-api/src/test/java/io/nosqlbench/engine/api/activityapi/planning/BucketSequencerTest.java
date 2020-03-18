package io.nosqlbench.engine.api.activityapi.planning;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;

public class BucketSequencerTest {

    @Test
    public void testBasicRatios() {
        BucketSequencer<String> buckets = new BucketSequencer<>();
        int[] ints = buckets.seqIndexesByRatios(List.of("a","b","c"), List.of(0L, 2L, 3L));
        assertThat(ints).containsExactly(1,2,1,2,2);
    }

}
