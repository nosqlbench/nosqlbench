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

package io.nosqlbench.virtdata.library.basics.core.stathelpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

public class DiscreteProbabilityBufferTest {

    private final static Logger logger = LogManager.getLogger(DiscreteProbabilityBufferTest.class);
    @Test
    public void testReplay() {
        DiscreteProbabilityBuffer dp = new DiscreteProbabilityBuffer(10);
        dp.add(1,2.0D);
        dp.add(2,2.0D);
        dp.add(3,4.0D);
        dp.add(4,8.0D);
        dp.add(5,16.0D);
        dp.add(6,32.0D);
        dp.add(7,64.0D);
        dp.add(8,128.0D);
        dp.add(9,256.0D);
        dp.add(10,512.0D);
        dp.normalize();

        for (DiscreteProbabilityBuffer.Entry entry : dp) {
            logger.debug("entry: " + entry.getEventId() + ":" + entry.getProbability());
        }
        logger.debug("cumuProb:" + dp.getCumulativeProbability());
    }

}
