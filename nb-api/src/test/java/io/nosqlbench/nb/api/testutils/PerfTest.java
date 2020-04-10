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

package io.nosqlbench.nb.api.testutils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PerfTest {

    @Test
    public void testBasics() {
        Perf p = new Perf("unittest");
        p.add("0",0,100,100);
        assertThat(p.isConverged(Result::getOpsPerSec,0.2d, 3)).isFalse();
        p.add("1",0,100,121);
        assertThat(p.isConverged(Result::getOpsPerSec,0.2d, 3)).isFalse();
        p.add("2",0,100,1421);
        assertThat(p.isConverged(Result::getOpsPerSec,0.2d, 3)).isFalse();
        p.add("3",0,100,1431);
        double[] deltas = p.getDeltas(Result::getOpsPerSec);
        System.out.println("Sanity Check for Perf methods:\n"+p.toStringDelta(Result::getOpsPerSec, "D_ops_s"));
        assertThat(p.isConverged(Result::getOpsPerSec,0.2d, 3)).isFalse();
        p.add("4",0,100,1441);
        assertThat(p.isConverged(Result::getOpsPerSec,0.2d, 3)).isTrue();
    }

}
