/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.library.basics.core.stathelpers.aliasmethod;

import io.nosqlbench.virtdata.library.basics.core.stathelpers.AliasSamplerDoubleInt;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.AliasSamplerDoubleLong;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.EvProbD;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.EvProbLongDouble;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AliasSamplerDoubleLongTest {

    private final static Logger logger = LogManager.getLogger(AliasSamplerDoubleLongTest.class);
    @Test
    public void testAliasSamplerBinaryFractions() {
        List<EvProbLongDouble> events = new ArrayList();
        events.add(new EvProbLongDouble(1L,1.0D));
        events.add(new EvProbLongDouble(2L,1.0D));
        events.add(new EvProbLongDouble(3L,2.0D));
        events.add(new EvProbLongDouble(4L,4.0D));
        events.add(new EvProbLongDouble(5L,8.0D));
        events.add(new EvProbLongDouble(6L,16.0D));
        events.add(new EvProbLongDouble(7L,32.0D));
        events.add(new EvProbLongDouble(8L,64.0D));

        AliasSamplerDoubleLong as = new AliasSamplerDoubleLong(events);
        int[] stats = new int[9];
        for (int i = 0; i < 10000; i++) {
            double v = (double)i / 10000D;
            long idx = as.applyAsLong(v);
            stats[(int)idx]++;
        }
        logger.debug(Arrays.toString(stats));
        assertThat(stats).containsExactly(0,79,79,157,313,626,1250,2499,4997);

    }

    @Test
    public void testAliasSamplerSimple() {
        List<EvProbD> events = new ArrayList<>();
        events.add(new EvProbD(1,1D));
        events.add(new EvProbD(2,2D));
        events.add(new EvProbD(3,3D));

        AliasSamplerDoubleInt as = new AliasSamplerDoubleInt(events);

        int[] stats = new int[4];
        for (int i = 0; i < 10000; i++) {
            double v = (double)i / 10000D;
            int idx = as.applyAsInt(v);
            stats[idx]++;
        }
        logger.debug(Arrays.toString(stats));
        assertThat(stats).containsExactly(0,1667,3334,4999);
    }



}
