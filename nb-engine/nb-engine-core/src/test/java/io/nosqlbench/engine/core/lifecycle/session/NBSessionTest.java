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

package io.nosqlbench.engine.core.lifecycle.session;

import io.nosqlbench.engine.cmdstream.Cmd;
import io.nosqlbench.engine.cmdstream.CmdArg;
import io.nosqlbench.nb.api.labels.NBLabeledElement;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
public class NBSessionTest implements NBLabeledElement {

    @Test
    public void testThatSessionShortCircuitsCommandErrors() {
        NBSession session = new NBSession(this, "session_name", Map.of());
        Cmd c1 = new Cmd("test_ok", Map.of("test_ok", CmdArg.of("test_ok","key1","===","value1")));
        Cmd c2 = new Cmd("test_error", Map.of());
        Cmd c3 = new Cmd("test_ok", Map.of("test_ok", CmdArg.of("test_ok","key2","===","value2")));
        List<Cmd> cmdStream = List.of(c1, c2, c3);

        session.apply(cmdStream);
    }

    @Override
    public NBLabels getLabels() {
        return NBLabels.forKV("testing","session");
    }
}
