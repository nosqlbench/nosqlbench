package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

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

import static org.assertj.core.api.Assertions.assertThat;

public class ToMD5ByteBufferTest {

    @Test
    public void testMD5String() {
        MD5HexString ms = new MD5HexString();
        String apply = ms.apply(3L);
        assertThat(apply).isEqualTo("1fb332efe1406a104b11ffa1fa04fa7a");
    }

}
