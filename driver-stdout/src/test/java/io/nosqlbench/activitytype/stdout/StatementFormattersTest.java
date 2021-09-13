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

package io.nosqlbench.activitytype.stdout;

import io.nosqlbench.adapters.stdout.TemplateFormat;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class StatementFormattersTest {

    @Test
    public void testCsvFormats() {
        String csv = TemplateFormat.csv.format(false,Arrays.asList("alpha", "beta"));
        assertThat(csv).isEqualTo("{alpha},{beta}");
    }

    @Test
    public void testInlineJSONFormat() {
        String csv = TemplateFormat.inlinejson.format(false,Arrays.asList("alpha", "beta"));
        assertThat(csv).isEqualTo("{\"alpha\":\"{alpha}\", \"beta\":\"{beta}\"}");
    }

    @Test
    public void testBlockJSONFormat() {
        String csv = TemplateFormat.json.format(false,Arrays.asList("alpha", "beta"));
        assertThat(csv).isEqualTo("{\n \"alpha\":\"{alpha}\",\n \"beta\":\"{beta}\"\n}");
    }

    @Test
    public void testAssignmentsFormat() {
        String csv = TemplateFormat.assignments.format(false,Arrays.asList("alpha", "beta"));
        assertThat(csv).isEqualTo("alpha={alpha} beta={beta}");
    }

    @Test
    public void testReadoutFormat() {
        String csv = TemplateFormat.readout.format(false,Arrays.asList("alpha", "beta"));
        assertThat(csv).isEqualTo("alpha : {alpha}\n beta : {beta}");
    }

}
