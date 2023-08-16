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

package io.nosqlbench.adapters.api.evalcontext;

import groovy.lang.MissingPropertyException;
import io.nosqlbench.virtdata.core.templates.ParsedTemplateString;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GroovyBooleanCycleFunctionTest {

    @Test
    public void testBasicScript() {
        ParsedTemplateString parsedTemplate = new ParsedTemplateString("""
            true;
            """,
            Map.of("numbername", "NumberNameToString()")
        );
        List<String> imports = List.of();
        GroovyBooleanCycleFunction function = new GroovyBooleanCycleFunction("test1", parsedTemplate, imports);
        Boolean result = function.apply(2);
        assertThat(result).isTrue();
    }

    @Test
    public void testUncaughtException() {
        ParsedTemplateString parsedTemplate = new ParsedTemplateString("""
            this_is_a_syntax_error
            """,
            Map.of("numbername", "NumberNameToString()")
        );
        List<String> imports = List.of();
        GroovyCycleFunction function = new GroovyBooleanCycleFunction("test2", parsedTemplate, imports);
        System.out.println(function);
        assertThatThrownBy(() -> function.apply(3L)).isInstanceOf(MissingPropertyException.class);
    }

}
