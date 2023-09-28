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

package io.nosqlbench.virtdata.library.basics.shared.vectors.primitive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

public class HashedDoubleVectorsTest {
    private final static Logger logger = LogManager.getLogger(HashedDoubleVectorsTest.class);
    @Test
    public void testHashedDoubleVectors() {
        HashedDoubleVectors hdv1 = new HashedDoubleVectors(10000);
        double[] doubles = hdv1.apply(1L);
        logger.info("created " + doubles.length + " double vectors.");
    }

}
