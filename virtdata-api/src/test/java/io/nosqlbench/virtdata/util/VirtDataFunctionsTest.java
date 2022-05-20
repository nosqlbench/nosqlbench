package io.nosqlbench.virtdata.util;

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


import io.nosqlbench.virtdata.api.bindings.VirtDataFunctions;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.function.Function;
import java.util.function.LongFunction;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class VirtDataFunctionsTest  {

    @Test
    public void testLongUnaryConversion() {
        Function<Long,Long> fl = (Long l) -> Math.max(l,43L);
        LongFunction<Long> adapted = VirtDataFunctions.adapt(fl, LongFunction.class, long.class, true);
        long f2 = adapted.apply(42L);
    }

    @Test
    public void testWrongLongUnaryConversion() {
        Function<Long,Integer> fl = (Long l) -> Math.max(l.intValue(), 43);
        assertThatExceptionOfType(InvalidParameterException.class)
                .isThrownBy(() -> VirtDataFunctions.adapt(fl, LongFunction.class, Long.class, true));
    }
}
