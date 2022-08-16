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

package io.nosqlbench.cli.testing;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class ProcessInvoker {
    private File runDirectory = new File(".");
    private File logDirectory = new File(".");

    public ProcessInvoker() {
    }

    /**
     * Run the process with a specified timeout and alias.
     *
     * @param alias          the name of the process for logging and result gathering
     * @param timeoutSeconds the number of seconds to wait for the process to return
     * @param cmdline        the command line
     * @return a ProcessResult
     */
    public ProcessResult run(String alias, int timeoutSeconds, String... cmdline) {

        ProcessResult result = new ProcessResult();
        result.stdout = getStdOutFile(alias);
        result.stderr = getStdErrFile(alias);
        long startNanosTime = 0;
        Process process = null;
        ProcessBuilder pb = new ProcessBuilder(cmdline);
        pb.redirectError(result.stderr);
        pb.redirectOutput(result.stdout);
        startNanosTime = System.nanoTime();

        try {
            result.cmdDir = new File(".").getCanonicalPath();
            process = pb.start();

            boolean terminated = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!terminated) {
                process.destroyForcibly().waitFor();
                result.exception = new RuntimeException("timed out waiting for process, so it was shutdown forcibly.");
            }
        } catch (Exception e) {
            result.exception = e;
        } finally {
            result.startNanosTime = startNanosTime;
            if (startNanosTime != 0) {
                long endNanosTime = System.nanoTime();
                result.durationNanos = endNanosTime - startNanosTime;
            }
            if (process != null) {
                result.exitStatus = process.exitValue();
            } else {
                result.exitStatus=255;
            }
        }
        return result;
    }

    public ProcessInvoker setLogDir(String logDir) {
        logDirectory = new File(logDir);
        return this;
    }

    public ProcessInvoker setRunDir(String runDir) {
        runDirectory = new File(runDir);
        return this;
    }

    public File getStdOutFile(String alias) {
        return mkdirsFor(new File(logDirectory.getPath() + File.separator + alias + ".stdout"));
    }

    public File getStdErrFile(String alias) {
        return mkdirsFor(new File(logDirectory.getPath() + File.separator + alias + ".stderr"));
    }

    private File mkdirsFor(File file) {
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new RuntimeException("Could not create directories for " + file);
            }
        }
        return file;
    }


}
