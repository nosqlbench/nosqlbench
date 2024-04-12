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

package io.nosqlbench.virtdata.userlibs.streams.fillers;

import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * If a type implements this interface, then it wants to be provided with a data source in the form of a series of byte
 * buffers. The source must provide additional byte buffers as they are requested. These byte buffers aren't required to
 * be any particular size. They aren't required to have distinct or special data. All that is required is that they
 * never run out.
 */
public interface ByteBufferFillable extends Fillable {
    void fill(Iterable<ByteBuffer> source);

    static void fillByteBuffer(ByteBuffer target, Iterable<ByteBuffer> source) {
        Iterator<ByteBuffer> iterator = source.iterator();
        target.clear();

        while (target.remaining() > 0 && iterator.hasNext()) {
            ByteBuffer next = iterator.next();
            if (next.remaining() > target.remaining()) {
                byte[] bytes = new byte[target.remaining()];
                next.get(bytes, 0, target.remaining());
                target.put(bytes);
            } else {
                target.put(next);
            }
            if (target.remaining() == 0) {
                break;
            }
        }
        target.flip();
    }

}
