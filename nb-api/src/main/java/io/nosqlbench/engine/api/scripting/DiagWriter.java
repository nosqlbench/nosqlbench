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

import org.jetbrains.annotations.NotNull;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiagWriter extends PrintWriter {

    private final DateTimeFormatter tsformat = DateTimeFormatter.ISO_DATE_TIME;
    InterjectingCharArrayWriter buffer;

    public DiagWriter(InterjectingCharArrayWriter charbuffer, Writer writer) {
        super(new FanWriter(charbuffer, writer));
        this.buffer = charbuffer;
    }

    public DiagWriter(InterjectingCharArrayWriter charbuffer) {
        super(charbuffer);
        this.buffer = charbuffer;
    }

    public DiagWriter(PrintWriter out) {
        super(out);
    }

    public String getBuf() {
        return this.buffer.toString();
    }

    private final static Pattern nl = Pattern.compile("([^\n]*\n?)");
    public String getTimedLog() {
        StringBuilder sb = new StringBuilder();
        long[] times = buffer.getTimes();
        int idx=0;
        Matcher finder = nl.matcher(buffer.toString());
        while (finder.find()) {
            String tsprefix = LocalDateTime.ofInstant(Instant.ofEpochMilli(times[idx++]), ZoneId.systemDefault()).format(tsformat);
            sb.append(tsprefix).append(" ").append(finder.group(0));
        }
        finder.appendTail(sb);
        return sb.toString();
    }
}
