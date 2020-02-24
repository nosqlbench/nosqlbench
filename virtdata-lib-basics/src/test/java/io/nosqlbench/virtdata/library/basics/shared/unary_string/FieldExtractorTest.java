package io.nosqlbench.virtdata.library.basics.shared.unary_string;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FieldExtractorTest {

    @Test
    public void testSubset() {
        FieldExtractor fieldExtractor = new FieldExtractor("|,2,3,5");
        assertThat(fieldExtractor.apply("one|two|three|four|five|six")).isEqualTo("two|three|five");
    }

    @Test
    public void testUnderrun() {
        FieldExtractor fieldExtractor = new FieldExtractor("|,2,3,5");
        assertThat(fieldExtractor.apply("one|two")).isEqualTo("ERROR-UNDERRUN in FieldExtractor");

    }
}