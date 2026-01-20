/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.mql.schema;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class LabelSetResolverTest {

    @Test
    void testEmptyLabels() {
        assertEquals("{}", LabelSetResolver.computeHash(Map.of()));
        assertEquals("{}", LabelSetResolver.computeHash(null));
    }

    @Test
    void testSingleLabel() {
        Map<String, String> labels = Map.of("activity", "write");
        assertEquals("{activity=write}", LabelSetResolver.computeHash(labels));
    }

    @Test
    void testMultipleLabels() {
        Map<String, String> labels = Map.of(
            "activity", "write",
            "host", "server1",
            "region", "us-east"
        );
        // Should be sorted alphabetically by key
        assertEquals("{activity=write,host=server1,region=us-east}",
                     LabelSetResolver.computeHash(labels));
    }

    @Test
    void testLabelsSortedAlphabetically() {
        // Insert in reverse order to test sorting
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("z_last", "value3");
        labels.put("b_second", "value2");
        labels.put("a_first", "value1");

        String hash = LabelSetResolver.computeHash(labels);
        assertEquals("{a_first=value1,b_second=value2,z_last=value3}", hash);
    }

    @Test
    void testSameLabelsDifferentOrder() {
        Map<String, String> labels1 = new LinkedHashMap<>();
        labels1.put("activity", "write");
        labels1.put("host", "server1");

        Map<String, String> labels2 = new LinkedHashMap<>();
        labels2.put("host", "server1");
        labels2.put("activity", "write");

        // Should produce same hash regardless of insertion order
        assertEquals(LabelSetResolver.computeHash(labels1),
                     LabelSetResolver.computeHash(labels2));
    }

    @Test
    void testSpecialCharacters() {
        Map<String, String> labels = Map.of(
            "label", "value=with=equals",
            "another", "value,with,commas"
        );
        String hash = LabelSetResolver.computeHash(labels);
        assertTrue(hash.contains("label=value=with=equals"));
        assertTrue(hash.contains("another=value,with,commas"));
    }
}
