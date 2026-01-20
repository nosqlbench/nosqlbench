package io.nosqlbench.virtdata.library.basics.shared.from_long.to_double;

/*
 * Copyright (c) nosqlbench
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
import org.junit.jupiter.api.Tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
public class LERPTest {

    @Test
    public void testDoubleLerp() {
        assertThat(LERP.lerp(10.0d,10.0d,1.0d)).isEqualTo(10.0d, Offset.offset(0.00001d));
        assertThat(LERP.lerp(10.0d,0.0d,0.0d)).isEqualTo(10.0d, Offset.offset(0.00001d));
        assertThat(LERP.lerp(10.0d,0.0d,1.0d)).isEqualTo(0.0d, Offset.offset(0.00001d));
        assertThat(LERP.lerp(10.0d,5.0d,0.5d)).isEqualTo(7.5d, Offset.offset(0.00001d));
    }

}
