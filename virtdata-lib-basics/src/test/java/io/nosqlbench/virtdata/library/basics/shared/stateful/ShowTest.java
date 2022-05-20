package io.nosqlbench.virtdata.library.basics.shared.stateful;

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


import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Save;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ShowTest {

    @Test
    public void testBasicStateSupport() {
        new Clear().apply(0L);
        io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Save saveFoo = new io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Save("foo");
        saveFoo.applyAsLong(23);
        new Save("cycle").applyAsLong(-1L);
        Show showAll=new Show();
        String shown = showAll.apply(234L);
        assertThat(shown).isEqualTo("{foo=23, cycle=-1}");
        io.nosqlbench.virtdata.library.basics.shared.unary_string.Save saveBar = new io.nosqlbench.virtdata.library.basics.shared.unary_string.Save("bar");
        saveBar.apply("Bar");
        Show showFoo = new Show("foo");
        Show showBar = new Show("bar");
        assertThat(showFoo.apply(2342343L)).isEqualTo("{foo=23}");
        assertThat(showBar.apply(23423L)).isEqualTo("{bar=Bar}");
        new Clear().apply(234);
        assertThat(showAll.apply("234")).isEqualTo("{}");
    }
}
