package io.nosqlbench.virtdata.library.basics.tests.long_long;

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


import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.SignedHash;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SignedHashTest {

    @Test
    public void testFunctionalResult() {

        SignedHash hash = new SignedHash();
        assertThat(hash.applyAsLong(0L)).isEqualTo(2945182322382062539L);
        assertThat(hash.applyAsLong(1L)).isEqualTo(6292367497774912474L);
        assertThat(hash.applyAsLong(2L)).isEqualTo(-8218881827949364593L);
        assertThat(hash.applyAsLong(3L)).isEqualTo(-8048510690352527683L);
        assertThat(hash.applyAsLong(0L)).isEqualTo(2945182322382062539L);
        assertThat(hash.applyAsLong(1L)).isEqualTo(6292367497774912474L);
        assertThat(hash.applyAsLong(2L)).isEqualTo(-8218881827949364593L);
        assertThat(hash.applyAsLong(3L)).isEqualTo(-8048510690352527683L);

    }

    @Test
    public void illustrateFirstTen() {

        SignedHash hash = new SignedHash();
        for (int i = 0; i < 10; i++) {
            long l = hash.applyAsLong(i) % 50L;
            System.out.println("i=" + i + " result=" + l);
        }

        for (int i = 0; i < 10; i++) {
            long l = hash.applyAsLong(i+1000000L) % 50L;
            System.out.println("i=" + i + " result=" + l);
        }

    }

}
