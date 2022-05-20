package io.nosqlbench.virtdata.userlibs.streams.fillers;

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


import java.nio.ByteBuffer;
import java.util.List;

/**
 * A fillable object needs to be filled with data. It may also have owned properties or objects that also need to be
 * filled with data. A type must be fillable if it is to be traversed to find other fillable elements.
 *
 * Fillable elements should expect to have whatever data they contain be replaced when they are filled.
 */
public interface Fillable {
    default List<Fillable> getFillables() {
        return List.of();
    }

    static void fill(Fillable fillable, ByteBufferSource source) {
        if (fillable instanceof ByteBufferFillable) {
            ((ByteBufferFillable) fillable).fill(source);
        } else {
            throw new RuntimeException("Unknown fillable type " + fillable.getClass().getCanonicalName());
        }
        for (Fillable fillableFillable : fillable.getFillables()) {
            fill(fillableFillable, source);
        }
    }
}
