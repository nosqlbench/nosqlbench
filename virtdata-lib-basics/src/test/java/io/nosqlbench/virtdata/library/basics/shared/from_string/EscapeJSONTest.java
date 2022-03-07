package io.nosqlbench.virtdata.library.basics.shared.from_string;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EscapeJSONTest {

    @Test
    public void testEscapes() {
        EscapeJSON escapeJSON = new EscapeJSON();
        assertThat(escapeJSON.apply("\t")).isEqualTo("\\t");
    }

}
