/*
 * Copyright (c) 2022-2023 nosqlbench
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
import io.nosqlbench.api.labels.NBLabeledElement;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AnnotationBuilderTest {

    private static final long time = 1600000000000L;

    @Test
    public void testBasicAnnotation() {

        Annotation an1 = Annotation.newBuilder()
            .element(NBLabeledElement.forKV("test_element","value"))
                .at(time)
                .layer(Layer.Scenario)
                .detail("detailk1", "detailv1")
                .detail("detailk2", "detailv21\ndetailv22")
                .detail("detailk3", "v1\nv2\nv3\n")
                .build();

        String represented = an1.toString();
        assertThat(represented).isEqualTo(
                """
                [2020-09-13T12:26:40Z]
                span:instant
                details:
                 detailk1: detailv1
                 detailk2:
                  detailv21
                  detailv22
                 detailk3:
                  v1
                  v2
                  v3
                labels:
                 test_element: value
                """);

    }

}
