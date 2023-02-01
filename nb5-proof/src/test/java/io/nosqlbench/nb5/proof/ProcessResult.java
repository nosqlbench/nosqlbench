/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.nb5.proof;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessResult {
    public int exitStatus = -1;
    public File stderr;
    public File stdout;
    public double durationNanos;
    public Exception exception;

    public static int NORMAL_EXIT = 0;
    public long startNanosTime;
    public String cmdDir;

    public boolean isNormal() {
        return exception==null && exitStatus == NORMAL_EXIT;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("--- process result\nstatus:%d", exitStatus));
        sb.append(String.format(" time:%.3fs", (durationNanos/1000000000.0)));
        sb.append(String.format(" dir:%s",cmdDir));
        sb.append("\n--- stdout:\n");
        sb.append(getStdoutData().stream().collect(Collectors.joining("\n")));
        sb.append("\n--- stderr:\n");
        sb.append(getStderrData().stream().collect(Collectors.joining("\n","","\n")));
        sb.append("---\n");
        return sb.toString();
    }

    public List<String> getStdoutData() {
        try {
            return Files.readAllLines(stdout.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getStderrData() {
        try {
            return Files.readAllLines(stderr.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
