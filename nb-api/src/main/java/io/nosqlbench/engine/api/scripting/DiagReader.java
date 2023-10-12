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
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DiagReader extends Reader {
    Reader wrapped;
    private final String prefix;
    final CharArrayWriter buffer;
    private final List<String> timedLog = new ArrayList<String>();

    private final DateTimeFormatter tsformat = DateTimeFormatter.ISO_DATE_TIME;



    public DiagReader(Reader wrapped, String prefix) {
        this.wrapped = wrapped;
        this.prefix = prefix;
        this.buffer = new CharArrayWriter(0);
    }

    public DiagReader(Reader wrapped) {
        this.wrapped = wrapped;
        this.prefix = null;
        this.buffer = null;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int read = wrapped.read(cbuf, off, len);
        if (buffer!=null) {
            String tsprefix = LocalDateTime.now().format(tsformat);
            buffer.write(cbuf, off, len);
            timedLog.add(tsprefix + prefix + new String(cbuf, off, len));
        }
        return read;
    }

    @Override
    public void close() throws IOException {
        wrapped.close();
        buffer.close();
    }

    public List<String> getTimedLog() {
        return this.timedLog;
    }
}
