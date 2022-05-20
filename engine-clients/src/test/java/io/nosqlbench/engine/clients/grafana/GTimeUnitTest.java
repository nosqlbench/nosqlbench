package io.nosqlbench.engine.clients.grafana;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GTimeUnitTest {

    @Test
    public void testParseBasic() {
        long result = GTimeUnit.epochSecondsFor("now");
    }

    @Test
    public void testParseRelative() {
        long result = GTimeUnit.epochSecondsFor("now-1w");
        assertThat(result).isCloseTo((System.currentTimeMillis() / 1000) - (86400L * 7L), Offset.offset(60L));
    }

}
