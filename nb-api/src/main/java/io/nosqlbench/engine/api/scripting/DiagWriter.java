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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DiagWriter extends PrintWriter {

    Writer wrapped;
    private final String prefix;
    CharArrayWriter buffer = new CharArrayWriter();
    private final List<String> timedLog = new ArrayList<String>();
    private final StringBuilder sb = new StringBuilder();
    private int checkpoint = 0;

    private final DateTimeFormatter tsformat = DateTimeFormatter.ISO_DATE_TIME;

    public DiagWriter(Writer wrapped, String prefix) {
        super(wrapped);
        this.wrapped = wrapped;
        this.prefix = prefix;
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        buffer.write(cbuf, off, len);
        String text = new String(cbuf, off, len);

        sb.append(text);
        checkpointIf();

        super.write(cbuf, off, len);
    }

    private void check() {
        if (sb.substring(checkpoint,sb.length()).contains("\n")) {
            checkpoint();
        }
    }

    private void checkpointIf() {
        if (checkpoint==sb.length()) {
            return;
        }
        if (sb.substring(checkpoint,sb.length()).contains("\n")) {
            checkpoint();
            checkpointIf();
        }
    }
    private void checkpoint() {
        String tsprefix = LocalDateTime.now().format(tsformat);
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
        checkpoint = 0;
    }

    @Override
    public void write(int c) {
        this.buffer.write(c);
        sb.append((char)c);
        checkpointIf();
        super.write(c);
    }

    @Override
    public void write(@NotNull char[] buf) {
        try {
            this.buffer.write(buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sb.append(buf);
        checkpointIf();
        super.write(buf);
    }

    @Override
    public void write(@NotNull String s, int off, int len) {
        this.buffer.write(s,off,len);
        sb.append(s);
        checkpointIf();
        super.write(s, off, len);
    }

    @Override
    public void write(@NotNull String s) {
        try {
            sb.append(s);
            this.buffer.write(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        checkpointIf();
        super.write(s);
    }

    @Override
    public void print(boolean b) {
        super.print(b);
    }

    @Override
    public void print(char c) {
        super.print(c);
    }

    @Override
    public void print(int i) {
        super.print(i);
    }

    @Override
    public void print(long l) {
        super.print(l);
    }

    @Override
    public void print(float f) {
        super.print(f);
    }

    @Override
    public void print(double d) {
        super.print(d);
    }

    @Override
    public void print(@NotNull char[] s) {
        super.print(s);
    }

    @Override
    public void print(String s) {
        super.print(s);
    }

    @Override
    public void print(Object obj) {
        super.print(obj);
    }

    @Override
    public void println() {
        super.println();
    }

    @Override
    public void println(boolean x) {
        super.println(x);
    }

    @Override
    public void println(char x) {
        super.println(x);
    }

    @Override
    public void println(int x) {
        super.println(x);
    }

    @Override
    public void println(long x) {
        super.println(x);
    }

    @Override
    public void println(float x) {
        super.println(x);
    }

    @Override
    public void println(double x) {
        super.println(x);
    }

    @Override
    public void println(@NotNull char[] x) {
        super.println(x);
    }

    @Override
    public void println(String x) {
        super.println(x);
    }

    @Override
    public void println(Object x) {
        super.println(x);
    }

    @Override
    public PrintWriter printf(@NotNull String format, Object... args) {
        return super.printf(format, args);
    }

    @Override
    public PrintWriter printf(Locale l, @NotNull String format, Object... args) {
        return super.printf(l, format, args);
    }

    @Override
    public PrintWriter format(@NotNull String format, Object... args) {
        return super.format(format, args);
    }

    @Override
    public PrintWriter format(Locale l, @NotNull String format, Object... args) {
        return super.format(l, format, args);
    }

    @Override
    public PrintWriter append(CharSequence csq) {
        return super.append(csq);
    }

    @Override
    public PrintWriter append(CharSequence csq, int start, int end) {
        return super.append(csq, start, end);
    }

    @Override
    public PrintWriter append(char c) {
        return super.append(c);
    }

    @Override
    public void flush() {
        buffer.flush();
        checkpoint();
        super.flush();
    }

    @Override
    public void close() {
        buffer.close();
        checkpoint();
        super.close();
    }

    public List<String> getTimedLog() {
        return timedLog;
    }
}
