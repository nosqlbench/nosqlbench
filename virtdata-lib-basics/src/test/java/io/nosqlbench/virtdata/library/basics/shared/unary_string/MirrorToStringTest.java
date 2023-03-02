package io.nosqlbench.virtdata.library.basics.shared.unary_string;

import io.nosqlbench.api.errors.BasicError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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
public class MirrorToStringTest {

    @Test
    void mirrorTest() {

        final MirrorToString mts = new MirrorToString("mirror-this");

        assertThat(mts.apply(new String())).isNotNull();
        assertThat(mts.apply(new String())).isEqualTo("mirror-this");
        assertThatExceptionOfType(BasicError.class).isThrownBy(() -> new MirrorToString(null));
    }


}