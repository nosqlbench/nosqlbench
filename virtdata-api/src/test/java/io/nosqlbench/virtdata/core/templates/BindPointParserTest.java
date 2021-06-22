package io.nosqlbench.virtdata.core.templates;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BindPointParserTest {

    @Test
    public void testBindPointParser() {
        BindPointParser bpp = new BindPointParser();
        assertThat(bpp.apply("test {one}")).containsExactly("test ","one","");
        assertThat(bpp.apply("test {one} {{two three}}")).containsExactly("test ","one"," ","two three","");
    }

    @Test
    public void testBindPointParserBypass() {
        BindPointParser bpp = new BindPointParser();
        assertThat(bpp.apply("")).containsExactly("");
    }

}
