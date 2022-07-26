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

package io.nosqlbench.converters.cql.exporters;

import io.nosqlbench.api.labels.Labeled;
import io.nosqlbench.cqlgen.core.CGElementNamer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class CGElementNamerTest {

    @Test
    public void testNonRequiredFields() {
        CGElementNamer namer = new CGElementNamer("[ABC---][,deFGH][__IJ__]");
        assertThat(namer.apply(Map.of())).isEqualTo("");
    }

    @Test
    public void testLiteralTweens() {
        CGElementNamer namer = new CGElementNamer("[ABC---]!-23[,deFGH])(*&[__IJ__]");
        assertThat(namer.apply(Map.of("abc","123"))).isEqualTo("123---!-23)(*&");
    }


    @Test
    public void testPartialFields() {
        CGElementNamer namer = new CGElementNamer("[ABC---][,deFGH][__IJ__]");
        assertThat(namer.apply(Map.of("abc", "base"))).isEqualTo("base---");
    }

    @Test
    public void testLabeledFields() {
        CGElementNamer namer = new CGElementNamer("[ABC---][,deFGH][__IJ__]");
        Labeled mylabeled = new Labeled() {
            @Override
            public Map<String, String> getLabels() {
                return Map.of("ij", "eyejay");
            }
        };
        assertThat(namer.apply(mylabeled, "abc", "base")).isEqualTo("base---__eyejay__");
    }

    @Test
    public void testCasedSectionName() {
        CGElementNamer namer = new CGElementNamer("[ABC---][,deFGH][__IJ__]");
        assertThat(namer.apply(
            Map.of(
                "abc", "1111",
                "fgh", "2222",
                "IJ", "3333"
            ))
        ).isEqualTo("1111---,de2222__3333__");
    }

    @Test
    public void testRequiredFieldsPresent() {
        CGElementNamer namer = new CGElementNamer("[ABC!---!]");
        Labeled mylabeled = new Labeled() {
            @Override
            public Map<String, String> getLabels() {
                return Map.of("ij", "eyejay");
            }
        };
        assertThat(namer.apply(Map.of(
            "abc", "WOOT"
        ))).isEqualTo("WOOT---!");

    }

    @Test
    public void testRequiredFieldsMissing() {
        CGElementNamer namer = new CGElementNamer("[ABC!---!]");
        Labeled mylabeled = new Labeled() {
            @Override
            public Map<String, String> getLabels() {
                return Map.of("ij", "eyejay");
            }
        };
        assertThatThrownBy(() -> namer.apply(Map.of(
            "not_the", "right_label"
        ))).isInstanceOf(RuntimeException.class);

    }

}
