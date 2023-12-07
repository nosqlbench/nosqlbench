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

import javax.script.SimpleScriptContext;
import java.io.Reader;
import java.io.Writer;

public class BufferedScriptCtx extends SimpleScriptContext {

    Reader reader;
    Writer writer;
    Writer errorWriter;

    public BufferedScriptCtx(Writer writer, Writer errorWriter, Reader reader) {
        this.writer = writer;
        this.errorWriter = errorWriter;
        this.reader = reader;
    }

    @Override
    public Reader getReader() { // stdin
        return this.reader;
    }

    @Override
    public Writer getWriter() { // stdout
        return this.writer;
    }

    @Override
    public Writer getErrorWriter() { // stderr
        return this.errorWriter;
    }

}
