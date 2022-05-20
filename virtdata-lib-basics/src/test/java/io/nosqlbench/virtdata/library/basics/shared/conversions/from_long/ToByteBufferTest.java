package io.nosqlbench.virtdata.library.basics.shared.conversions.from_long;

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


import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

public class ToByteBufferTest {

    @Test
    public void toByteBuffer7() {
        ToByteBuffer f = new ToByteBuffer(7);
        ByteBuffer byteBuffer = f.apply(33);
        ByteBuffer expected = ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 0});
        assertThat(byteBuffer).isEqualByComparingTo(expected);
    }

    @Test
    public void toByteBuffer8() {
        ToByteBuffer f = new ToByteBuffer(8);
        ByteBuffer byteBuffer = f.apply(33);
        ByteBuffer expected = ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 0, 33});
        assertThat(byteBuffer).isEqualByComparingTo(expected);
    }

    @Test
    public void toByteBuffer9() {
        ToByteBuffer f = new ToByteBuffer(9);
        ByteBuffer byteBuffer = f.apply(33);
        ByteBuffer expected = ByteBuffer.wrap(new byte[]{0, 0, 0, 0, 0, 0, 0, 33, 0});
        assertThat(byteBuffer).isEqualByComparingTo(expected);
    }

}
