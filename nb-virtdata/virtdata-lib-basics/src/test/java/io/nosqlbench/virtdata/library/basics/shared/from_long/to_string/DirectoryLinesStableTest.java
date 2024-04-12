/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DirectoryLinesStableTest {

    @Test
    public void testStableOrdering() {
        DirectoryLinesStable directoryLines = new DirectoryLinesStable("./src/test/resources/static-do-not-change", ".+txt");
        assertThat(directoryLines.apply(0)).isEqualTo("data1.txt-line1");
        assertThat(directoryLines.apply(0)).isEqualTo("data1.txt-line1");
        assertThat(directoryLines.apply(4)).isEqualTo("data1.txt-line5");
        assertThat(directoryLines.apply(5)).isEqualTo("data2.txt-line1");
        assertThat(directoryLines.apply(9)).isEqualTo("data2.txt-line5");
        assertThat(directoryLines.apply(9)).isEqualTo("data2.txt-line5");
        assertThat(directoryLines.apply(10)).isEqualTo("data1.txt-line1");
        assertThat(directoryLines.apply(14)).isEqualTo("data1.txt-line5");
        assertThat(directoryLines.apply(15)).isEqualTo("data2.txt-line1");
        assertThat(directoryLines.apply(19)).isEqualTo("data2.txt-line5");
        assertThat(directoryLines.apply(Long.MAX_VALUE)).isEqualTo("data2.txt-line3");
    }

//    @Test
//    public void testOverRangeIssue() {
//        DirectoryLinesStable directoryLines = new DirectoryLinesStable(
//            "exampledata/local/testdirlines", ".+jsonl"
//        );
//        for (long i = 0; i < 40000; i++) {
//            String result = directoryLines.apply(i);
//        }
//
//    }


}
