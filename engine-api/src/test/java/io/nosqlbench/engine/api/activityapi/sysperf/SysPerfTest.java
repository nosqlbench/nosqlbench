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

package io.nosqlbench.engine.api.activityapi.sysperf;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.attribute.FileTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class SysPerfTest {

    @Test
    @Disabled
    public void testForcedBench() {

//        SysPerf.get().reset();

        SysPerf.get().getPerfData(false);
        Optional<FileTime> cacheFileTime1 = SysPerf.get().getCacheFileTime();
        assertThat(cacheFileTime1).isPresent();
        long run1Time = cacheFileTime1.get().toMillis();

        SysPerf.get().getPerfData(true);
        Optional<FileTime> cacheFileTime2 = SysPerf.get().getCacheFileTime();
        assertThat(cacheFileTime2).isPresent();
        long run2Time = cacheFileTime2.get().toMillis();

        assertThat(run1Time).isLessThan(run2Time);

        SysPerf.get().getPerfData(false);
        Optional<FileTime> cacheFileTime3 = SysPerf.get().getCacheFileTime();
        assertThat(cacheFileTime3).isPresent();
        long run3Time = cacheFileTime3.get().toMillis();

        assertThat(run2Time).isEqualTo(run3Time);
    }

}
