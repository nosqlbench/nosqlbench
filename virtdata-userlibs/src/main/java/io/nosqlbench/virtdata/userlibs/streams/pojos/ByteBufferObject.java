package io.nosqlbench.virtdata.userlibs.streams.pojos;

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


import io.nosqlbench.virtdata.userlibs.streams.fillers.ByteBufferFillable;
import io.nosqlbench.virtdata.userlibs.streams.fillers.Fillable;

import java.nio.ByteBuffer;
import java.util.List;

public class ByteBufferObject implements ByteBufferFillable {

    private final ByteBuffer buffer;

    public ByteBufferObject(int size) {
        this.buffer = ByteBuffer.allocate(size);
    }
    public ByteBufferObject(ByteBuffer source) {
        this.buffer = source;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    @Override
    public void fill(Iterable<ByteBuffer> source) {
        ByteBufferFillable.fillByteBuffer(this.buffer,source);
    }
}
