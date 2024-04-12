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

package io.nosqlbench.virtdata.userlibs.streams.pojos;

import io.nosqlbench.virtdata.userlibs.streams.fillers.ByteBufferSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ByteBufferFillableTest {

    @Test
    public void testBytesFillableFromLargeBuffers() {
        ByteBufferObject a = new ByteBufferObject(537);
        ByteBufferSource byteBuffers = new ByteBufferSource(0, Long.MAX_VALUE, 1024 * 1024);
        a.fill(byteBuffers);
        assertThat(a.getBuffer().capacity()).isEqualTo(537);
        assertThat(a.getBuffer().position()).isEqualTo(0);
    }

    @Test
    public void testBytesFillableFromSmallBuffers() {
        ByteBufferObject a = new ByteBufferObject(537);
        ByteBufferSource byteBuffers = new ByteBufferSource(0, Long.MAX_VALUE, 37);
        a.fill(byteBuffers);
        assertThat(a.getBuffer().capacity()).isEqualTo(537);
        assertThat(a.getBuffer().position()).isEqualTo(0);
    }

}
