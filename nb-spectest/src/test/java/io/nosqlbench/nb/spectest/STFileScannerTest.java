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

package io.nosqlbench.nb.spectest;

import io.nosqlbench.nb.spectest.loaders.STFileScanner;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class STFileScannerTest {

    @Test
    public void findTestSpecs() {
        List<Path> found = STFileScanner.findMatching(".*\\.md", Path.of("src/test/resources"));
        assertThat(found).contains(Path.of("src/test/resources/spectestdir/scanner-test-file.md"));
    }

}
