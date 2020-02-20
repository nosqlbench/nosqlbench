package io.virtdata.libbasics.shared.from_long.to_collection;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SetTest {

    @Test
    public void testSet() {
        Set set = new Set(s -> 3, e -> e);
        java.util.Set<Object> s1 = set.apply(15L);
        assertThat(s1).containsOnly(15L,16L,17L);
    }

    @Test
    public void testStringSet() {
        StringSet set = new StringSet(s -> 3, e -> e);
        java.util.Set<String> s1 = set.apply(15L);
        assertThat(s1).containsOnly("15","16","17");
    }
}