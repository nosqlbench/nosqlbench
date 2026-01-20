/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.core.templates;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("unit")
public class CapturePointParserTest {

    @Test
    public void testCapturePoint1() {
        CapturePointParser cpp = new CapturePointParser();
        CapturePointParser.Result result = cpp.apply("string with [capture1]");
        assertThat(result).isEqualTo(
            new CapturePointParser.Result(
                "string with capture1",
                new CapturePoints(List.of(CapturePoint.of("capture1")))
            )
        );
    }

    @Test
    public void testCapturePointWithCast() {
        CapturePointParser cpp = new CapturePointParser();
        CapturePointParser.Result result = cpp.apply("string with [(Number)capture1]");
        assertThat(result).isEqualTo(
            new CapturePointParser.Result(
                "string with capture1",
                new CapturePoints(List.of(CapturePoint.of(Number.class,"capture1", "capture1")))
            )
        );
        CapturePoint cp1 = result.getCaptures().get(0);
        assertThat(cp1.toString()).isEqualTo("[(Number)capture1]");

        assertThat(cp1.valueOf(Long.valueOf(42L))).isEqualTo(42L);
        assertThatThrownBy(() -> cp1.valueOf("42L")).hasMessageContaining("Cannot cast java.lang.String to java.lang.Number");
    }

    @Test
    public void testCaptureWildcard() {
        CapturePointParser cpp = new CapturePointParser();
        CapturePointParser.Result result = cpp.apply("select [*] from foobarbaz");
        assertThat(result).isEqualTo(
            new CapturePointParser.Result(
                "select * from foobarbaz",
                new CapturePoints(List.of(CapturePoint.of(Object.class, "*", "*")))
            )
        );
    }

    @Test
    public void testMapEquivalence() {
        CapturePointParser cpp = new CapturePointParser();
        CapturePointParser.Result result = cpp.parse(Map.of("asname","(Integer) varname"));
        assertThat(result).isEqualTo(
            new CapturePointParser.Result(
                "varname",
                new CapturePoints(List.of(CapturePoint.of(Integer.class, "varname", "asname")))
            )
        );

    }
}
