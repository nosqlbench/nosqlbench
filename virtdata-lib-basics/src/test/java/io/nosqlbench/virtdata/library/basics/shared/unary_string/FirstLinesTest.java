/*
 * Copyright (c) 2023 nosqlbench
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
package io.nosqlbench.virtdata.library.basics.shared.unary_string;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;

class FirstLinesTest {

    private static final String EXPECTED_SINGLELINE_CONTENTS = "test-data-entry";
    private static final String EXPECTED_MULTILINE_CONTENTS = "additional information";
    private static final String NOT_EXPECTED_CONTENTS = "foozy-content";
    private static final String VALID_PATH = "text-provider-sample.txt";
    private static final String INVALID_PATH = "not-good.txt";
    private static final Object PLACEHOLDER_APPLY_INPUT = new Object();


    @Test
    void testValidPathAndContents() {
        final FirstLines firstLines = new FirstLines(VALID_PATH);
        assertThat(firstLines.apply(PLACEHOLDER_APPLY_INPUT).trim()).isEqualTo(EXPECTED_SINGLELINE_CONTENTS);
    }

    @Test
    void testInvalidPath() {
        final FirstLines firstLinesValid = new FirstLines(VALID_PATH);
        assertThatException().isThrownBy(() -> new FirstLines(INVALID_PATH));
        assertThat(firstLinesValid.apply(PLACEHOLDER_APPLY_INPUT)).isNotEqualTo(NOT_EXPECTED_CONTENTS);
    }

    @Test
    void testFullContentsAndSingleLine() {

        FirstLines allLines = new FirstLines(VALID_PATH, 8675309);
        assertThat(allLines.apply(PLACEHOLDER_APPLY_INPUT)).isNotEmpty();

        final FirstLines textOfEntireFile = new FirstLines(VALID_PATH, 3);
        assertThat(textOfEntireFile.apply(PLACEHOLDER_APPLY_INPUT)).contains(EXPECTED_MULTILINE_CONTENTS);

        final FirstLines textOfSingleLine = new FirstLines(VALID_PATH, 1);
        assertThat(textOfSingleLine.apply(PLACEHOLDER_APPLY_INPUT).trim()).isEqualTo(EXPECTED_SINGLELINE_CONTENTS);
    }

}
