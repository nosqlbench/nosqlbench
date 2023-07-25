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

package io.nosqlbench.engine.api.activityapi.cyclelog.inputs.cyclelog;

import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.CycleSegment;
import io.nosqlbench.engine.api.activityapi.cyclelog.outputs.cyclelog.CycleLogOutput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class CycleLogInputTest {

    private final static String filepath="cycle-log-reader-test";
    private static File cyclefile;

    @BeforeAll
    public static void createTempFile() {
        try {
            cyclefile = File.createTempFile(filepath, "cyclelog");
            System.out.println("tmp file for testing:" + cyclefile.getPath());
            cyclefile.deleteOnExit();

        CycleLogOutput out = new CycleLogOutput(cyclefile, 10);
        out.onCycleResult(1L,11);
        out.onCycleResult(2L,22);
        out.onCycleResult(3L,33);
        out.onCycleResult(4L,44);
        out.onCycleResult(5L,55);
        out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(cyclefile).exists();
    }

    @Test
    public void testReader() {
        CycleLogInput cycleLogInput = new CycleLogInput(cyclefile.getPath());
        CycleSegment i1;
        long c;
        i1 = cycleLogInput.getInputSegment(1);
        c = i1.nextCycle();
        assertThat(c).isEqualTo(1L);
        i1 = cycleLogInput.getInputSegment(1);
        c = i1.nextCycle();
        assertThat(c).isEqualTo(2L);
        i1 = cycleLogInput.getInputSegment(1);
        c = i1.nextCycle();
        assertThat(c).isEqualTo(3L);
        i1 = cycleLogInput.getInputSegment(1);
        c = i1.nextCycle();
        assertThat(c).isEqualTo(4L);
        i1 = cycleLogInput.getInputSegment(1);
        c = i1.nextCycle();
        assertThat(c).isEqualTo(5L);
        assertThat(i1.isExhausted()).isTrue();
    }

}
