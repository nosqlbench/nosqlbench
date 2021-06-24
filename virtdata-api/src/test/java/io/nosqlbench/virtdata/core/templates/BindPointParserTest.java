package io.nosqlbench.virtdata.core.templates;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class BindPointParserTest {

    @Test
    public void testSingleRefTypeBindPoint() {
        BindPointParser bpp = new BindPointParser();
        assertThat(bpp.apply("test {one}", Map.of())).isEqualTo(
            new BindPointParser.Result(
                List.of("test ","one",""),
                List.of(BindPoint.of("one",null, BindPoint.Type.reference)))
        );
//        assertThat(bpp.apply("test {one} {{two three}}",Map.of())).containsExactly("test ","one"," ","two three","");
    }

    @Test
    public void testSingleDefinitionTypeBindPoint() {
        BindPointParser bpp = new BindPointParser();
        assertThat(bpp.apply("test {{this is a definition}}", Map.of())).isEqualTo(
            new BindPointParser.Result(
                List.of("test ","this is a definition",""),
                List.of(BindPoint.of(BindPointParser.DEFINITION,"this is a definition", BindPoint.Type.definition)))
        );

    }

}
