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

package io.nosqlbench.engine.core.lifecycle.scenario.script;

import io.nosqlbench.engine.core.lifecycle.scenario.context.NBSceneBuffer;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBSceneFixtures;

import javax.script.SimpleScriptContext;
import java.io.Reader;
import java.io.Writer;

public class BufferedScriptContext extends SimpleScriptContext {
    private final NBSceneFixtures fixtures;

    public BufferedScriptContext(NBSceneFixtures fixtures) {
        this.fixtures = fixtures;
    }

    @Override
    public Reader getReader() { // stdin
        return fixtures.in();
    }

    @Override
    public Writer getWriter() { // stdout
        return fixtures.out();
    }

    @Override
    public Writer getErrorWriter() { // stderr
        return fixtures.err();
    }

}
