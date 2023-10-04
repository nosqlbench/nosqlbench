/*
 * Copyright (c) 2022-2023 nosqlbench
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
package io.nosqlbench.engine.api.scripting;

import javax.script.SimpleScriptContext;
import java.io.Reader;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ScriptEnvBuffer provides a variant of SimpleScriptContext which captures all
 * stdin, stdout, and stderr data into diagnostic character buffers.
 */
public class ScriptEnvBuffer extends SimpleScriptContext {

    private DiagWriter stdoutBuffer;
    private DiagWriter stderrBuffer;
    private DiagReader stdinBuffer;

    private final DateTimeFormatter tsformat = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public Writer getWriter() {
        if (stdoutBuffer==null) {
            synchronized(this) {
                if (stdoutBuffer==null) {
                    Writer superWriter = super.getWriter();
                    stdoutBuffer = new DiagWriter(superWriter, " stdout ");
                }
            }
        }
        return stdoutBuffer;
    }

    @Override
    public Writer getErrorWriter() {
        if (stderrBuffer==null) {
            synchronized(this) {
                if (stderrBuffer==null) {
                    Writer superErrorWriter = super.getErrorWriter();
                    stderrBuffer = new DiagWriter(superErrorWriter, " error ");
                }
            }
        }
        return stderrBuffer;
    }

    @Override
    public Reader getReader() {
        if (stdinBuffer == null) {
            synchronized (this) {
                if (stdinBuffer == null) {
                    Reader superReader = super.getReader();
                    stdinBuffer = new DiagReader(superReader, " stdin ");
                }
            }
        }
        return stdinBuffer;
    }

    public String getStdinText() {
        return stdinBuffer.buffer.toString();
    }

    public String getStderrText() {
        return stderrBuffer.buffer.toString();
    }

    public String getStdoutText() {
        return stdoutBuffer.buffer.toString();
    }

    public List<String> getTimeLogLines() {
        List<String> log = new ArrayList<String>();
        Optional.ofNullable(this.stdinBuffer).map(DiagReader::getTimedLog).ifPresent(log::addAll);
        Optional.ofNullable(this.stderrBuffer).map(DiagWriter::getTimedLog).ifPresent(log::addAll);
        Optional.ofNullable(this.stdoutBuffer).map(DiagWriter::getTimedLog).ifPresent(log::addAll);
        log = log.stream().map(l -> l.endsWith("\n") ? l : l+"\n").collect(Collectors.toList());
        return log;
    }
    public String getTimedLog() {
        return getTimeLogLines().stream().collect(Collectors.joining());
    }

}
