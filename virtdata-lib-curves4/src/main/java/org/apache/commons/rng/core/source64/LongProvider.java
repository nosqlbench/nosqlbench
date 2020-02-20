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

package org.apache.commons.rng.core.source64;

import org.apache.commons.rng.core.BaseProvider;
import org.apache.commons.rng.core.util.NumberFactory;

/**
 * Base class for all implementations that provide a {@code long}-based
 * source randomness.
 */
public abstract class LongProvider
    extends BaseProvider
    implements RandomLongSource {

    /**
     * Provides a bit source for booleans.
     *
     * <p>A cached value from a call to {@link #nextLong()}.
     */
    private long booleanSource; // Initialised as 0

    /**
     * The bit mask of the boolean source to obtain the boolean bit.
     *
     * <p>The bit mask contains a single bit set. This begins at the least
     * significant bit and is gradually shifted upwards until overflow to zero.
     *
     * <p>When zero a new boolean source should be created and the mask set to the
     * least significant bit (i.e. 1).
     */
    private long booleanBitMask; // Initialised as 0

    /**
     * Provides a source for ints.
     *
     * <p>A cached value from a call to {@link #nextLong()}.
     */
    private long intSource;

    /** Flag to indicate an int source has been cached. */
    private boolean cachedIntSource; // Initialised as false

    /** {@inheritDoc} */
    @Override
    protected byte[] getStateInternal() {
        // Pack the boolean inefficiently as a long
        final long[] state = new long[] { booleanSource,
                                          booleanBitMask,
                                          intSource,
                                          cachedIntSource ? 1 : 0 };
        return composeStateInternal(super.getStateInternal(),
                                    NumberFactory.makeByteArray(state));
    }

    /** {@inheritDoc} */
    @Override
    protected void setStateInternal(byte[] s) {
        final byte[][] c = splitStateInternal(s, 32);
        final long[] state = NumberFactory.makeLongArray(c[0]);
        booleanSource   = state[0];
        booleanBitMask  = state[1];
        intSource       = state[2];
        // Non-zero is true
        cachedIntSource = state[3] != 0;
        super.setStateInternal(c[1]);
    }

    /** {@inheritDoc} */
    @Override
    public long nextLong() {
        return next();
    }

    /** {@inheritDoc} */
    @Override
    public int nextInt() {
        // Directly store and use the long value as a source for ints
        if (cachedIntSource) {
            // Consume the cache value
            cachedIntSource = false;
            // Return the lower 32 bits
            return NumberFactory.extractLo(intSource);
        }
        // Fill the cache
        cachedIntSource = true;
        intSource = nextLong();
        // Return the upper 32 bits
        return NumberFactory.extractHi(intSource);
    }

    /** {@inheritDoc} */
    @Override
    public double nextDouble() {
        return NumberFactory.makeDouble(nextLong());
    }

    /** {@inheritDoc} */
    @Override
    public boolean nextBoolean() {
        // Shift up. This will eventually overflow and become zero.
        booleanBitMask <<= 1;
        // The mask will either contain a single bit or none.
        if (booleanBitMask == 0) {
            // Set the least significant bit
            booleanBitMask = 1;
            // Get the next value
            booleanSource = nextLong();
        }
        // Return if the bit is set
        return (booleanSource & booleanBitMask) != 0;
    }

    /** {@inheritDoc} */
    @Override
    public float nextFloat() {
        return NumberFactory.makeFloat(nextInt());
    }

    /** {@inheritDoc} */
    @Override
    public void nextBytes(byte[] bytes) {
        nextBytesFill(this, bytes, 0, bytes.length);
    }

    /** {@inheritDoc} */
    @Override
    public void nextBytes(byte[] bytes,
                          int start,
                          int len) {
        checkIndex(0, bytes.length - 1, start);
        checkIndex(0, bytes.length - start, len);

        nextBytesFill(this, bytes, start, len);
    }

    /**
     * Generates random bytes and places them into a user-supplied array.
     *
     * <p>
     * The array is filled with bytes extracted from random {@code long} values.
     * This implies that the number of random bytes generated may be larger than
     * the length of the byte array.
     * </p>
     *
     * @param source Source of randomness.
     * @param bytes Array in which to put the generated bytes. Cannot be null.
     * @param start Index at which to start inserting the generated bytes.
     * @param len Number of bytes to insert.
     */
    static void nextBytesFill(RandomLongSource source,
                              byte[] bytes,
                              int start,
                              int len) {
        int index = start; // Index of first insertion.

        // Index of first insertion plus multiple of 8 part of length
        // (i.e. length with 3 least significant bits unset).
        final int indexLoopLimit = index + (len & 0x7ffffff8);

        // Start filling in the byte array, 8 bytes at a time.
        while (index < indexLoopLimit) {
            final long random = source.next();
            bytes[index++] = (byte) random;
            bytes[index++] = (byte) (random >>> 8);
            bytes[index++] = (byte) (random >>> 16);
            bytes[index++] = (byte) (random >>> 24);
            bytes[index++] = (byte) (random >>> 32);
            bytes[index++] = (byte) (random >>> 40);
            bytes[index++] = (byte) (random >>> 48);
            bytes[index++] = (byte) (random >>> 56);
        }

        final int indexLimit = start + len; // Index of last insertion + 1.

        // Fill in the remaining bytes.
        if (index < indexLimit) {
            long random = source.next();
            while (true) {
                bytes[index++] = (byte) random;
                if (index < indexLimit) {
                    random >>>= 8;
                } else {
                    break;
                }
            }
        }
    }
}
