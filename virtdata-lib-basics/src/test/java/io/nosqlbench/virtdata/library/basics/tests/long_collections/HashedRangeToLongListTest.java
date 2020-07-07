package io.nosqlbench.virtdata.library.basics.tests.long_collections;

import io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection.HashedRangeToLongList;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HashedRangeToLongListTest {

    @Test
    public void longListRangeTest() {
        HashedRangeToLongList gener = new HashedRangeToLongList(3, 6, 9, 12);
        for (int i = 0; i < 100; i++) {
            System.out.println("long list range test size: " + i);
            List<Long> list= gener.apply(i);
            assertThat(list.size()).isBetween(9,12);
            for (Long longVal : list) {
                assertThat(longVal.longValue()).isBetween(3L,6L);
            }
        }
    }

}