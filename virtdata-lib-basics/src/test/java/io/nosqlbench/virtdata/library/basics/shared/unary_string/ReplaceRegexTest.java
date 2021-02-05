package io.nosqlbench.virtdata.library.basics.shared.unary_string;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReplaceRegexTest {

    @Test
    public void testRegexReplacer() {
        ReplaceRegex two = new ReplaceRegex("[one]", "two");
        String replaced = two.apply("one");
        assertThat(replaced).isEqualTo("twotwotwo");
    }

    @Test
    public void testReplaceString() {
        ReplaceAll replaceAll = new ReplaceAll("one", "two");
        String replaced = replaceAll.apply("onetwothree");
        assertThat(replaced).isEqualTo("twotwothree");

    }

}
