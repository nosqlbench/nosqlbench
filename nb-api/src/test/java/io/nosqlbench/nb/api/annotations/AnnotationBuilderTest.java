package io.nosqlbench.nb.api.annotations;

import io.nosqlbench.nb.api.Layer;
import org.junit.Test;

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
                "[Sun Sep 13 07:26:40 CDT 2020]\n" +
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
                " labelkc: labelvd\n");

    }

}