/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.activitytype.diag;

import org.junit.jupiter.api.Test;

import java.io.PrintStream;

public class SequenceBlockerTest {

    @Test
    public void await() throws Exception {

        SequenceBlocker sb = new SequenceBlocker(234L, true);
        new Thread(() -> sb.awaitAndRun(249L,253L, new Printer(System.out, "249-253"))).start();
        Thread.sleep(100);
        new Thread(() -> sb.awaitAndRun(247L,249L, new Printer(System.out, "247-249"))).start();
        Thread.sleep(100);
        new Thread(() -> sb.awaitAndRun(234L,247L, new Printer(System.out, "234-247"))).start();

        sb.awaitCompletion();
        System.out.flush();
    }

    private final static class Printer implements Runnable {

        private PrintStream printStream;
        private final String out;

        public Printer(PrintStream printStream, String out) {
            this.printStream = printStream;
            this.out = out;
        }

        @Override
        public void run() {
            printStream.println(out);
        }
    }


}