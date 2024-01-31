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

package io.nosqlbench.nbr.examples.injava;

import io.nosqlbench.engine.cmdstream.Cmd;
import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import io.nosqlbench.engine.core.lifecycle.session.CmdParser;
import io.nosqlbench.engine.core.lifecycle.session.NBSession;
import io.nosqlbench.nb.api.labels.NBLabeledElement;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DirectNBSessionTests {

    @Test
    public void testThatSessionShortCircuitsOnFailedCommand() {
        LinkedList<Cmd> cmds = CmdParser.parseArgvCommands(new LinkedList<>(List.of("ok", "id=1", "error", "id=2","ok", "id=3")));
        try (NBSession session = new NBSession(NBLabeledElement.EMPTY, "shortcircuit", Map.of())) {
            final ExecutionResult[] result = new ExecutionResult[1];
            assertThrows(RuntimeException.class, () -> session.apply(cmds).rethrow());
        }

    }
}
