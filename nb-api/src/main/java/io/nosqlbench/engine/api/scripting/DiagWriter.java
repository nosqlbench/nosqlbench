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

package io.nosqlbench.engine.api.scripting;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DiagWriter extends PrintWriter {

    Writer wrapped;
    private final String prefix;
    CharArrayWriter buffer = new CharArrayWriter();
    private final List<String> timedLog = new ArrayList<String>();
    private final StringBuilder sb = new StringBuilder();

    private final DateTimeFormatter tsformat = DateTimeFormatter.ISO_DATE_TIME;

    public DiagWriter(Writer wrapped, String prefix) {
        super(wrapped);
        this.wrapped = wrapped;
        this.prefix = prefix;
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        String tsprefix = LocalDateTime.now().format(tsformat);

        buffer.write(cbuf, off, len);
        String text = new String(cbuf, off, len);

        sb.append(text);

        if (text.contains("\n")) {
            String msgs = sb.toString();
            String extra = msgs.substring(msgs.lastIndexOf("\n") + 1);
            sb.setLength(0);
            sb.append(extra);
            String[] parts = msgs.substring(0, msgs.length() - extra.length()).split("\n");
            for (String part : parts) {
                if (!part.isBlank()) {
                    String tslogEntry = tsprefix + prefix + part + "\n";
                    timedLog.add(tslogEntry);
                }
            }

        }

        super.write(cbuf, off, len);
    }

    @Override
    public void flush() {
        buffer.flush();
        super.flush();
    }

    @Override
    public void close() {
        buffer.close();
        super.close();
    }

    public List<String> getTimedLog() {
        return timedLog;
    }
}
