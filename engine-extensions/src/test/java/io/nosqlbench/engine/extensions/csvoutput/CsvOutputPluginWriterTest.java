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

package io.nosqlbench.engine.extensions.csvoutput;

import org.assertj.core.util.Files;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

public class CsvOutputPluginWriterTest {

    @Test
    public void testCsvOutputWriter() {
        File tmpfile = Files.newTemporaryFile();
        tmpfile.deleteOnExit();
        System.out.println("tmpfile="+ tmpfile.getPath());
        CsvOutputPluginWriter out = new CsvOutputPluginWriter(tmpfile.getPath(), "one", "two");
        out.write(Value.asValue(Map.of("one","one_","two","two_")));
    }


}
