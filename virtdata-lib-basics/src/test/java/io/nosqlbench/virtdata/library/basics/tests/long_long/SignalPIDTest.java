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

package io.nosqlbench.virtdata.library.basics.tests.long_long;

import io.nosqlbench.virtdata.library.basics.shared.unary_int.SignalPID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SignalPIDTest {
    private final static Logger logger = LogManager.getLogger(SignalPIDTest.class);

    @Test
    public void testSignalPIDIdentity() {
        final Long currentProcessId = ProcessHandle.current().pid();
        assertThat(new SignalPID().apply(1L)).isEqualTo(currentProcessId);
        assertThat(new SignalPID().apply(2L)).isEqualTo(currentProcessId);
    }

}
