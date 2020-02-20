/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.rng.simple.internal;

import org.apache.commons.rng.core.util.NumberFactory;

import java.util.Arrays;

/**
 * Creates a {@code long[]} from a {@code byte[]}.
 *
 * @since 1.0
 */
public class ByteArray2LongArray implements SeedConverter<byte[], long[]> {
    /** Number of bytes in a {@code long}. */
    private static final int LONG_SIZE = 8;

    /** {@inheritDoc} */
    @Override
    public long[] convert(byte[] seed) {
        final byte[] tmp = seed.length % LONG_SIZE == 0 ?
            seed :
            Arrays.copyOf(seed, LONG_SIZE * ((seed.length + LONG_SIZE - 1) / LONG_SIZE));

        return NumberFactory.makeLongArray(tmp);
    }
}
