package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import org.junit.Test;

import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

import static org.assertj.core.api.Assertions.assertThat;

public class SetTest {

    @Test
    public void testSet() {
        Set set = new Set((LongToIntFunction) s -> 3, (LongFunction<Object>) e -> e);
        java.util.Set<Object> s1 = set.apply(15L);
        assertThat(s1).containsOnly(15L,16L,17L);
    }

    @Test
    public void testStringSet() {
        StringSet set = new StringSet((LongToIntFunction) s -> 3, (LongToIntFunction) (e -> (int)e));
        java.util.Set<String> s1 = set.apply(15L);
        assertThat(s1).containsOnly("15","16","17");
    }
}