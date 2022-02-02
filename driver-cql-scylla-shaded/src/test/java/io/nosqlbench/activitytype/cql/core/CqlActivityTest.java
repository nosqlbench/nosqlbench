package io.nosqlbench.activitytype.cql.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CqlActivityTest {

    @Test
    public void testCanonicalize() {
        String cb = CqlActivity.canonicalizeBindings("A ?b C");
        assertThat(cb).isEqualTo("A {b} C");
    }
}
