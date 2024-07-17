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

package io.nosqlbench.engine.cli.atfiles;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class NBAtFileTest {

    @Test
    public void testParseSimpleListDefaultFmt() {
        LinkedList<String> strings = NBAtFile.includeAt("@src/test/resources/atfiles/simple_list.yaml");
        assertThat(strings).containsExactly("arg1","arg2","arg3");
    }

    @Test
    public void testRelativizedPaths() {
        LinkedList<String> strings = NBAtFile.includeAt("@src/test/resources/atfiles/relativized.yaml");
        assertThat(strings).containsExactly("--option1=src/test/resources/atfiles/value1");
    }

    @Test
    public void testParseSimpleMapDefaultFmt() {
        LinkedList<String> strings = NBAtFile.includeAt("@src/test/resources/atfiles/simple_map.yaml");
        assertThat(strings).containsExactly("arg1=val1","arg2=val2","arg3=val3");
    }

    @Test
    public void testThatEmptyPathWithPathSpecifierIsInvalid() {
        assertThrows(RuntimeException.class, () -> NBAtFile.includeAt("@src/test/resources/atfiles/simple_map.yaml:>:"));
    }

    @Test
    public void testParseSimpleMapWithFormatter() {
        LinkedList<String> strings = NBAtFile.includeAt("@src/test/resources/atfiles/simple_map.yaml>:");
        assertThat(strings).containsExactly("arg1:val1","arg2:val2","arg3:val3");
    }


    @Test
    public void testParseSimpleMapSlashesOrDots() {
        LinkedList<String> strings = NBAtFile.includeAt("@src/test/resources/atfiles/mixed_structures.yaml:amap/ofamap.ofalist");
        assertThat(strings).containsExactly("option1","option2");
    }

    @Test
    public void testMapPathWithColonFormat() {
        LinkedList<String> strings = NBAtFile.includeAt("@src/test/resources/atfiles/mixed_structures.yaml:amap/ofamap.ofentries>:");
        assertThat(strings).containsExactly("key1:value1","key2:value2");
    }

    @Test
    public void testMapPathWithEqualsFormat() {
        LinkedList<String> strings = NBAtFile.includeAt("@src/test/resources/atfiles/mixed_structures.yaml:amap/ofamap.ofentries>=");
        assertThat(strings).containsExactly("key1=value1","key2=value2");
    }

    @Test
    public void testGlobalOptionForms() {
        LinkedList<String> strings = NBAtFile.includeAt("@src/test/resources/atfiles/global_opts.yaml>--");
        assertThat(strings).containsExactly("--option1", "--option2=value2", "--option3=value3", "--option4=value4");
    }

    @Test
    public void testAtfileSimpleRecursion() {
        LinkedList<String> strings = NBAtFile.includeAt("@src/test/resources/atfiles/simple_recursion.yaml");
        assertThat(strings).containsExactly("arg1","arg1","arg2","arg3","arg3");
    }

    @Test
    public void testAtfileDoubleRecursion() {
        LinkedList<String> strings = NBAtFile.includeAt("@src/test/resources/atfiles/double_recursion.yaml");
        assertThat(strings).containsExactly("arg1","arg1","arg1","arg2","arg3","arg3","arg3","deepval");
    }

}
