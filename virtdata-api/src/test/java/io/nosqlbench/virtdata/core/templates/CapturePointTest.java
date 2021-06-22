package io.nosqlbench.virtdata.core.templates;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CapturePointTest {

    @Test
    public void testBasicCaptures() {
        CapturePointParser cpp = new CapturePointParser();
        assertThat(cpp.apply("test [point1] [point2 as alias3]")).isEqualTo(
            new CapturePointParser.Result("test point1 point2",
                List.of(
                    CapturePoint.of("point1"),
                    CapturePoint.of("point2","alias3")
                ))
        );
    }

    @Test
    public void testBypass() {
        CapturePointParser cpp = new CapturePointParser();
        assertThat(cpp.apply("")).isEqualTo(
            new CapturePointParser.Result("", List.of())
        );
    }

}
