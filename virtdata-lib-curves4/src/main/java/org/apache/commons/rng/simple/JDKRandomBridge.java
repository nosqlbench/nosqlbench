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
package org.apache.commons.rng.simple;

import org.apache.commons.rng.RestorableUniformRandomProvider;
import org.apache.commons.rng.core.RandomProviderDefaultState;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

/**
 * Subclass of {@link Random} that {@link #next(int) delegates} to a
 * {@link RestorableUniformRandomProvider} instance but will otherwise rely
 * on the base class for generating all the random types.
 *
 * <p>
 * Legacy applications coded against the JDK's API could use this subclass
 * of {@link Random} in order to replace its linear congruential generator
 * by any {@link RandomSource}.
 * </p>
 *
 * Caveat: Use of this class is <em>not</em> recommended for new applications.
 * In particular, there is no guarantee that the serialized form of this class
 * will be compatible across (even <em>minor</em>) releases of the library.
 *
 * @since 1.0
 */
public final class JDKRandomBridge extends Random {
    /** Serializable version identifier. */
    private static final long serialVersionUID = 20161107L;
    /** Source. */
    private final RandomSource source;
    /** Delegate. */
    private transient RestorableUniformRandomProvider delegate;
    /** Workaround JDK's "Random" bug: https://bugs.openjdk.java.net/browse/JDK-8154225 */
    private final transient boolean isInitialized;

    /**
     * Creates a new instance.
     *
     * @param source Source of randomness.
     * @param seed Seed.  Can be {@code null}.
     */
    public JDKRandomBridge(RandomSource source,
                           Object seed) {
        this.source = source;
        delegate = RandomSource.create(source, seed);
        isInitialized = true;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void setSeed(long seed) {
        if (isInitialized) {
            delegate = RandomSource.create(source, seed);

            // Force the clearing of the "haveNextNextGaussian" flag
            // (cf. Javadoc of the base class); the value passed here
            // is irrelevant (since it will not be used).
            super.setSeed(0L);
        }
    }

    /**
     * Delegates the generation of 32 random bits to the
     * {@code RandomSource} argument provided at
     * {@link #JDKRandomBridge(RandomSource,Object) construction}.
     * The returned value is such that if the source of randomness is
     * {@link RandomSource#JDK}, all the generated values will be identical
     * to those produced by the same sequence of calls on a {@link Random}
     * instance initialized with the same seed.
     *
     * @param n Number of random bits which the requested value must contain.
     * @return the value represented by the {@code n} high-order bits of a
     * pseudo-random 32-bits integer.
     */
    @Override
    protected synchronized int next(int n) {
        return delegate.nextInt() >>> (32 - n);
    }

    /**
     * @param out Output stream.
     * @throws IOException if an error occurs.
     */
    private synchronized void writeObject(ObjectOutputStream out)
        throws IOException {
        // Write non-transient fields.
        out.defaultWriteObject();

        // Save current state.
        out.writeObject(((RandomProviderDefaultState) delegate.saveState()).getState());
   }

    /**
     * @param in Input stream.
     * @throws IOException if an error occurs.
     * @throws ClassNotFoundException if an error occurs.
     */
    private void readObject(ObjectInputStream in)
        throws IOException,
               ClassNotFoundException {
        // Read non-transient fields.
        in.defaultReadObject();

        // Recreate the "delegate" from serialized info.
        delegate = RandomSource.create(source);
        // And restore its state.
        delegate.restoreState(new RandomProviderDefaultState((byte[]) in.readObject()));
    }
}
