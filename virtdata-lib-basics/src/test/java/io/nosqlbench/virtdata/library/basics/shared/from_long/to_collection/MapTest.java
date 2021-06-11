package io.nosqlbench.virtdata.library.basics.shared.from_long.to_collection;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MapTest {

    @Test
    public void testMap() {
        Map mf = new Map((s) -> (int) s, (k) -> k, (v) -> v);
        java.util.Map<Object, Object> m1 = mf.apply(1L);
        assertThat(m1).containsOnlyKeys(1L);
        assertThat(m1).containsValues(1L);
    }

    @Test
    public void testStringMap() {
        StringMap sm = new StringMap((s)->2,(k)->k,(v)->v);
        java.util.Map<String, String> m2 = sm.apply(11L);
        assertThat(m2).containsOnlyKeys("11","12");
        assertThat(m2).containsValues("11","12");
    }

    @Test
    public void testMapTuple() {
        Map mf = new Map(s1 -> (int) s1, k2 -> (int) k2, s2 -> (int) s2, k2 -> (int)k2);
        java.util.Map<Object, Object> mt = mf.apply(37L);
        assertThat(mt).containsOnlyKeys(37,38);
        assertThat(mt).containsValues(37,38);
    }

    @Test
    public void testStringMapTuple() {
        StringMap mf = new StringMap(s1 -> (int) s1, k2 -> (int) k2, s2 -> (int) s2, k2 -> (int)k2);
        java.util.Map<String, String> mt = mf.apply(37L);
        assertThat(mt).containsOnlyKeys("37","38");
        assertThat(mt).containsValues("37","38");
    }

}