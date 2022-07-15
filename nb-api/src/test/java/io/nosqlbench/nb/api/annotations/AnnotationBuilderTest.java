/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.nb.api.annotations;

import io.nosqlbench.api.annotations.Annotation;
import io.nosqlbench.api.annotations.Layer;
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
