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

class TextOfFileTest {

    private static final String EXPECTED_CONTENTS = "test-data-entry";
    private static final String EXPECTED_MULTILINE_CONTENTS = "additional information";
    private static final String NOT_EXPECTED_CONTENTS = "foozy-content";
    private static final String VALID_PATH = "text-provider-sample.txt";
    private static final String INVALID_PATH = "not-good.txt";
    private static final String PLACEHOLDER_APPLY_INPUT = "placeholder-input";


    @Test
    void testValidPathAndContents() {
        final TextOfFile TextOfFile = new TextOfFile(VALID_PATH);
        assertThat(TextOfFile.apply(PLACEHOLDER_APPLY_INPUT).trim()).isEqualTo(EXPECTED_CONTENTS);
    }

    @Test
    void testInvalidPathAndSingleLineContents() {
        final TextOfFile textOfFileValid = new TextOfFile(VALID_PATH);
        assertThatException().isThrownBy(() -> new TextOfFile(INVALID_PATH));
        assertThat(textOfFileValid.apply(PLACEHOLDER_APPLY_INPUT)).isNotEqualTo(NOT_EXPECTED_CONTENTS);
    }

    @Test
    void testFullContentsAndSingleLine() {

        boolean isFirstLineOnly = false;
        final TextOfFile textOfEntireFile = new TextOfFile(VALID_PATH, isFirstLineOnly);
        assertThat(textOfEntireFile.apply(PLACEHOLDER_APPLY_INPUT)).contains(EXPECTED_MULTILINE_CONTENTS);

        isFirstLineOnly = true;
        final TextOfFile textOfSingleLine = new TextOfFile(VALID_PATH, isFirstLineOnly);
        assertThat(textOfSingleLine.apply(PLACEHOLDER_APPLY_INPUT).trim()).isEqualTo(EXPECTED_CONTENTS);
    }

}
