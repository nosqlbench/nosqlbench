package io.nosqlbench.virtdata.library.basics.shared.from_long.to_uuid;

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


import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash;

import java.util.UUID;
import java.util.function.LongFunction;

/**
 * This function provides a stable hashing of the input value to
 * a version 4 (Random) UUID.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class ToHashedUUID implements LongFunction<UUID> {

    private final Hash longHash = new Hash();

    @Override
    public UUID apply(long value) {
        long msbs = longHash.applyAsLong(value);
        long lsbs = longHash.applyAsLong(value+1);
        msbs = (msbs & 0xFFFFFFFFFFFF0FFFL) | 0x0000000000004000L; // It's not worth doing byte-level ops.
        lsbs = (lsbs & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L; // It all gets handled as longs anyway.
        UUID uuid= new UUID(msbs,lsbs);
        return uuid;
    }
}
