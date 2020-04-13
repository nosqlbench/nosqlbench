/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.api.activityconfig.rawyaml;

import io.nosqlbench.virtdata.core.bindings.DataMapper;
import io.nosqlbench.virtdata.core.bindings.VirtData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BindingEscapingTest {

    private final static Logger logger = LoggerFactory.getLogger(BindingEscapingTest.class);

    @Test
    public void testThatEscapesAreNotUnescapedInSingleQuotes() {
        DataMapper<String> mapper = VirtData.getMapper(
            "Template('\"-{}-\"Func(234)\\\"\\)',NumberNameToString());"
        );
        String s = mapper.get(234);
        assertThat(s).isEqualTo("\"-two hundred and thirty four-\"Func(234)\\\"\\)");
    }

    @Test
    public void testThatEscapesAreUnescapedInSingleQuotes() {
        DataMapper<String> mapper =
            VirtData.getMapper(
                "Template(\"\\\"-{}-\\\"Func(234)\\\")\",NumberNameToString());"
            );
        String s = mapper.get(234);
        assertThat(s).isEqualTo("\"-two hundred and thirty four-\"Func(234)\")");
    }

}
