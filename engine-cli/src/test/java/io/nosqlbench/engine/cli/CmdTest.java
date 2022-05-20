package io.nosqlbench.engine.cli;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CmdTest {

    private final static PathCanonicalizer p = new PathCanonicalizer();

    @Test
    public void testCmdForWaitMillis() {

        Cmd cmd = Cmd.parseArg(new LinkedList<String>(List.of("waitmillis", "234")), p);
        assertThat(cmd.getArg("millis_to_wait")).isEqualTo("234");
        assertThat(cmd.toString()).isEqualTo("waitMillis('234');");
    }

    @Test
    public void testCmdForStart() {
        Cmd cmd = Cmd.parseArg(new LinkedList<>(List.of("start","type=stdout","otherparam=foo")),p);
        assertThat(cmd.toString()).isEqualTo("start({\n" +
            "    'type':       'stdout',\n" +
            "    'otherparam': 'foo'\n" +
            "});");
    }

}
