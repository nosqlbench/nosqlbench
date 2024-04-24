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

package io.nosqlbench.nb.api.config.standard;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NBConfigSplitterTest {

    @Test
    public void testSplitConfigs() {
        List<String> strings = NBConfigSplitter.splitConfigLoaders("http://config.example.com/asdf,file:foo,{inline config},c://file,/tmp/test");
        assertThat(strings).containsExactly(
            "http://config.example.com/asdf",
            "file:foo",
            "{inline config}",
            "c://file",
            "/tmp/test"
        );
    }

    @Test
    public void testSplitConfigs2() {
        List<String> strings = NBConfigSplitter.splitConfigLoaders("http://config.example.com/asdf,{\"inline1\":\"config1\"},{\"inline2\":\"config2\"}");
        assertThat(strings).containsExactly(
            "http://config.example.com/asdf",
            "{\"inline1\":\"config1\"}",
            "{\"inline2\":\"config2\"}"
        );
    }

}
