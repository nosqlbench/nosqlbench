package io.nosqlbench.nb.api.annotations;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AnnotationBuilderTest {

    private static final long time = 1600000000000L;

    @Test
    public void testBasicAnnotation() {

        Annotation an1 = Annotation.newBuilder()
                .session("test-session")
                .at(time)
                .layer(Layer.Scenario)
                .label("labelka", "labelvb")
                .label("labelkc", "labelvd")
                .detail("detailk1", "detailv1")
                .detail("detailk2", "detailv21\ndetailv22")
                .detail("detailk3", "v1\nv2\nv3\n")
                .build();

        String represented = an1.toString();
        assertThat(represented).isEqualTo("session: test-session\n" +
                "[2020-09-13T12:26:40Z]\n" +
                "span:instant\n" +
                "details:\n" +
                " detailk1: detailv1\n" +
                " detailk2: \n" +
                "  detailv21\n" +
                "  detailv22\n" +
                " detailk3: \n" +
                "  v1\n" +
                "  v2\n" +
                "  v3\n" +
                "labels:\n" +
                " layer: Scenario\n" +
                " labelka: labelvb\n" +
                " labelkc: labelvd\n" +
                " session: test-session\n" +
                " span: instant\n" +
                " appname: nosqlbench\n");

    }

}