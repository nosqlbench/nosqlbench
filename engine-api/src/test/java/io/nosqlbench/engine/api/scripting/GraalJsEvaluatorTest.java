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

package io.nosqlbench.engine.api.scripting;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class GraalJsEvaluatorTest {

    @Test
    public void testBasicOperations() {
        GraalJsEvaluator<Long> ne = new GraalJsEvaluator<>(Long.class);
        ne.put("one", 1L).put("two", 2L);
        Long sum = ne.script("one + two;").eval();
        assertThat(sum).isEqualTo(3L);
    }

    @Test
    public void testJavaReturnType() {
        GraalJsEvaluator<Date> dateEval = new GraalJsEvaluator<>(Date.class);
        dateEval.script("var d = new java.util.Date(234); d;");
        Date aDate = dateEval.eval();
        assertThat(aDate).isEqualTo(new Date(234));
    }

    @Test
    public void testOneLiner() {
        String result = new GraalJsEvaluator<String>(String.class)
                .put("fname", "afirstname", "lname", "alastname")
                .script("fname + lname").eval();
        assertThat(result).isEqualTo("afirstnamealastname");
    }

}
