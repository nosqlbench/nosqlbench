package io.nosqlbench.virtdata.core.templates;

import io.nosqlbench.virtdata.core.bindings.ValueType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ValueTypeTest {

    @Test
    public void testMatchingRawObject() {
        ValueType vt = ValueType.valueOfClassName("Object");
        assertThat(vt).isEqualTo(ValueType.OBJECT);

    }

}
