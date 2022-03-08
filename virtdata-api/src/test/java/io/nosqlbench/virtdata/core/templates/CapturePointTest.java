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

package io.nosqlbench.virtdata.core.templates;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CapturePointTest {

    @Test
    public void testBasicCaptures() {
        CapturePointParser cpp = new CapturePointParser();
        assertThat(cpp.apply("test [point1] [point2 as alias3]")).isEqualTo(
            new CapturePointParser.Result("test point1 point2",
                List.of(
                    CapturePoint.of("point1"),
                    CapturePoint.of("point2","alias3")
                ))
        );
    }

    @Test
    public void testBypass() {
        CapturePointParser cpp = new CapturePointParser();
        assertThat(cpp.apply("")).isEqualTo(
            new CapturePointParser.Result("", List.of())
        );
    }

}
