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

package org.apache.commons.rng.core;

import org.apache.commons.rng.RandomProviderState;
import org.apache.commons.rng.RestorableUniformRandomProvider;

/**
 * Base class with default implementation for common methods.
 */
public abstract class BaseProvider
    implements RestorableUniformRandomProvider {
    /** {@inheritDoc} */
    @Override
    public int nextInt(int n) {
        checkStrictlyPositive(n);

        if ((n & -n) == n) {
            return (int) ((n * (long) (nextInt() >>> 1)) >> 31);
        }
        int bits;
        int val;
        do {
            bits = nextInt() >>> 1;
            val = bits % n;
        } while (bits - val + (n - 1) < 0);

        return val;
    }

    /** {@inheritDoc} */
    @Override
    public long nextLong(long n) {
        checkStrictlyPositive(n);

        long bits;
        long val;
        do {
            bits = nextLong() >>> 1;
            val  = bits % n;
        } while (bits - val + (n - 1) < 0);

        return val;
    }

    /** {@inheritDoc} */
    @Override
    public RandomProviderState saveState() {
        return new RandomProviderDefaultState(getStateInternal());
    }

    /** {@inheritDoc} */
    @Override
    public void restoreState(RandomProviderState state) {
        if (state instanceof RandomProviderDefaultState) {
            setStateInternal(((RandomProviderDefaultState) state).getState());
        } else {
            throw new IllegalArgumentException("Foreign instance");
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getClass().getName();
    }

    /**
     * Combine parent and subclass states.
     * This method must be called by all subclasses in order to ensure
     * that state can be restored in case some of it is stored higher
     * up in the class hierarchy.
     *
     * I.e. the body of the overridden {@link #getStateInternal()},
     * will end with a statement like the following:
     * <pre>
     *  <code>
     *    return composeStateInternal(state,
     *                                super.getStateInternal());
     *  </code>
     * </pre>
     * where {@code state} is the state needed and defined by the class
     * where the method is overridden.
     *
     * @param state State of the calling class.
     * @param parentState State of the calling class' parent.
     * @return the combined state.
     * Bytes that belong to the local state will be stored at the
     * beginning of the resulting array.
     */
    protected byte[] composeStateInternal(byte[] state,
                                          byte[] parentState) {
        final int len = parentState.length + state.length;
        final byte[] c = new byte[len];
        System.arraycopy(state, 0, c, 0, state.length);
        System.arraycopy(parentState, 0, c, state.length, parentState.length);
        return c;
    }

    /**
     * Splits the given {@code state} into a part to be consumed by the caller
     * in order to restore its local state, while the reminder is passed to
     * the parent class.
     *
     * I.e. the body of the overridden {@link #setStateInternal(byte[])},
     * will contain statements like the following:
     * <pre>
     *  <code>
     *    final byte[][] s = splitState(state, localStateLength);
     *    // Use "s[0]" to recover the local state.
     *    super.setStateInternal(s[1]);
     *  </code>
     * </pre>
     * where {@code state} is the combined state of the calling class and of
     * all its parents.
     *
     * @param state State.
     * The local state must be stored at the beginning of the array.
     * @param localStateLength Number of elements that will be consumed by the
     * locally defined state.
     * @return the local state (in slot 0) and the parent state (in slot 1).
     * @throws IllegalStateException if {@code state.length < localStateLength}.
     */
    protected byte[][] splitStateInternal(byte[] state,
                                          int localStateLength) {
        checkStateSize(state, localStateLength);

        final byte[] local = new byte[localStateLength];
        System.arraycopy(state, 0, local, 0, localStateLength);
        final int parentLength = state.length - localStateLength;
        final byte[] parent = new byte[parentLength];
        System.arraycopy(state, localStateLength, parent, 0, parentLength);

        return new byte[][] { local, parent };
    }

    /**
     * Creates a snapshot of the RNG state.
     *
     * @return the internal state.
     */
    protected byte[] getStateInternal() {
        // This class has no state (and is the top-level class that
        // declares this method).
        return new byte[0];
    }

    /**
     * Resets the RNG to the given {@code state}.
     *
     * @param state State (previously obtained by a call to
     * {@link #getStateInternal()}).
     * @throws IllegalStateException if the size of the given array is
     * not consistent with the state defined by this class.
     *
     * @see #checkStateSize(byte[],int)
     */
    protected void setStateInternal(byte[] state) {
        if (state.length != 0) {
            // This class has no state.
            throw new IllegalStateException("State not fully recovered by subclasses");
        }
    }

    /**
     * Simple filling procedure.
     * It will
     * <ol>
     *  <li>
     *   fill the beginning of {@code state} by copying
     *   {@code min(seed.length, state.length)} elements from
     *   {@code seed},
     *  </li>
     *  <li>
     *   set all remaining elements of {@code state} with non-zero
     *   values (even if {@code seed.length < state.length}).
     *  </li>
     * </ol>
     *
     * @param state State. Must be allocated.
     * @param seed Seed. Cannot be null.
     */
    protected void fillState(int[] state,
                             int[] seed) {
        final int stateSize = state.length;
        final int seedSize = seed.length;
        System.arraycopy(seed, 0, state, 0, Math.min(seedSize, stateSize));

        if (seedSize < stateSize) {
            for (int i = seedSize; i < stateSize; i++) {
                state[i] = (int) (scrambleWell(state[i - seed.length], i) & 0xffffffffL);
            }
        }
    }

    /**
     * Simple filling procedure.
     * It will
     * <ol>
     *  <li>
     *   fill the beginning of {@code state} by copying
     *   {@code min(seed.length, state.length)} elements from
     *   {@code seed},
     *  </li>
     *  <li>
     *   set all remaining elements of {@code state} with non-zero
     *   values (even if {@code seed.length < state.length}).
     *  </li>
     * </ol>
     *
     * @param state State. Must be allocated.
     * @param seed Seed. Cannot be null.
     */
    protected void fillState(long[] state,
                             long[] seed) {
        final int stateSize = state.length;
        final int seedSize = seed.length;
        System.arraycopy(seed, 0, state, 0, Math.min(seedSize, stateSize));

        if (seedSize < stateSize) {
            for (int i = seedSize; i < stateSize; i++) {
                state[i] = scrambleWell(state[i - seed.length], i);
            }
        }
    }

    /**
     * Checks that the {@code state} has the {@code expected} size.
     *
     * @param state State.
     * @param expected Expected length of {@code state} array.
     * @throws IllegalStateException if {@code state.length < expected}.
     * @deprecated Method is used internally and should be made private in
     * some future release.
     */
    @Deprecated
    protected void checkStateSize(byte[] state,
                                  int expected) {
        if (state.length < expected) {
            throw new IllegalStateException("State size must be larger than " +
                                            expected + " but was " + state.length);
        }
    }

    /**
     * Checks whether {@code index} is in the range {@code [min, max]}.
     *
     * @param min Lower bound.
     * @param max Upper bound.
     * @param index Value that must lie within the {@code [min, max]} interval.
     * @throws IndexOutOfBoundsException if {@code index} is not within the
     * {@code [min, max]} interval.
     */
    protected void checkIndex(int min,
                              int max,
                              int index) {
        if (index < min ||
            index > max) {
            throw new IndexOutOfBoundsException(index + " is out of interval [" +
                                                min + ", " +
                                                max + "]");
        }
    }

    /**
     * Checks that the argument is strictly positive.
     *
     * @param n Number to check.
     * @throws IllegalArgumentException if {@code n <= 0}.
     */
    private void checkStrictlyPositive(long n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Must be strictly positive: " + n);
        }
    }

    /**
     * Transformation used to scramble the initial state of
     * a generator.
     *
     * @param n Seed element.
     * @param mult Multiplier.
     * @param shift Shift.
     * @param add Offset.
     * @return the transformed seed element.
     */
    private static long scramble(long n,
                                 long mult,
                                 int shift,
                                 int add) {
        // Code inspired from "AbstractWell" class.
        return mult * (n ^ (n >> shift)) + add;
    }

    /**
     * Transformation used to scramble the initial state of
     * a generator.
     *
     * @param n Seed element.
     * @param add Offset.
     * @return the transformed seed element.
     * @see #scramble(long,long,int,int)
     */
    private static long scrambleWell(long n,
                                     int add) {
        // Code inspired from "AbstractWell" class.
        return scramble(n, 1812433253L, 30, add);
    }
}
