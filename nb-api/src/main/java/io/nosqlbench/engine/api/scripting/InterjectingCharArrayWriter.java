package io.nosqlbench.engine.api.scripting;

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


import org.jetbrains.annotations.NotNull;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InterjectingCharArrayWriter extends CharArrayWriter {
    private final DateTimeFormatter tsformat = DateTimeFormatter.ISO_DATE_TIME;
    private final String prefix;
    private int offset = 0;
    private long[] times = new long[10];
    private int timeidx = 0;

    public InterjectingCharArrayWriter(String prefix) {
        this.prefix = prefix;
    }

    public long[] getTimes() {
        return times;
    }
    @Override
    public void write(int c) {
        super.write(c);
        markTime();
    }

    private void markTime() {
        long now = -1L;
        if (offset == 0) {
            now = appendTime(-1);
        }
        if (count > offset) {
            for (int i = offset; i < count; i++) {
                if (buf[i]=='\n') {
                    appendTime(now);
                }
            }
        }
        offset=count;
    }

    private long appendTime(long time) {
        if (time < 0) {
            time = System.currentTimeMillis();
        }
        if (times.length <= timeidx) {
            long[] realloc = new long[times.length << 1];
            System.arraycopy(times, 0, realloc, 0, times.length);
            this.times = realloc;
        }
        this.times[timeidx++] = time;
        return time;
    }

    @Override
    public void writeTo(Writer out) throws IOException {
        super.writeTo(out);
        markTime();
    }

    @Override
    public void write(char[] c, int off, int len) {
        super.write(c, off, len);
        markTime();
    }

    @Override
    public void write(@NotNull char[] cbuf) throws IOException {
        super.write(cbuf);
        markTime();
    }

    @Override
    public void write(@NotNull String str) throws IOException {
        super.write(str);
        markTime();
    }

    @Override
    public void write(String str, int off, int len) {
        super.write(str,off,len);
        markTime();
    }
}
