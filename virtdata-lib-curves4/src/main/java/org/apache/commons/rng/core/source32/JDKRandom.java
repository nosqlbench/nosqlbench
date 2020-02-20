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
package org.apache.commons.rng.core.source32;

import java.io.*;
import java.util.Random;

/**
 * A provider that uses the {@link Random#nextInt()} method of the JDK's
 * {@link Random} class as the source of randomness.
 *
 * <p>
 * <b>Caveat:</b> All the other calls will be redirected to the methods
 * implemented within this library.
 * </p>
 *
 * <p>
 * The state of this source of randomness is saved and restored through
 * the serialization of the {@link Random} instance.
 * </p>
 *
 * @since 1.0
 */
public class JDKRandom extends IntProvider {
    /** Delegate.  Cannot be "final" (to allow serialization). */
    private Random delegate;
    /** Size of the byte representation of the state (of the delegate). */
    private int stateSize;

    /**
     * Creates an instance with the given seed.
     *
     * @param seed Initial seed.
     */
    public JDKRandom(Long seed) {
        delegate = new Random(seed);
    }

    /**
     * {@inheritDoc}
     *
     * @see Random#nextInt()
     */
    @Override
    public int next() {
        return delegate.nextInt();
    }

    /** {@inheritDoc} */
    @Override
    protected byte[] getStateInternal() {
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(bos);

            // Serialize the "delegate".
            oos.writeObject(delegate);

            final byte[] state = bos.toByteArray();
            stateSize = state.length; // To allow state recovery.
            return composeStateInternal(state,
                                        super.getStateInternal());
        } catch (IOException e) {
            // Workaround checked exception.
            throw new IllegalStateException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void setStateInternal(byte[] s) {
        final byte[][] c = splitStateInternal(s, stateSize);

        try {
            final ByteArrayInputStream bis = new ByteArrayInputStream(c[0]);
            final ObjectInputStream ois = new ObjectInputStream(bis);

            delegate = (Random) ois.readObject();
        } catch (ClassNotFoundException e) {
            // Workaround checked exception.
            throw new IllegalStateException(e);
        } catch (IOException e) {
            // Workaround checked exception.
            throw new IllegalStateException(e);
        }

        super.setStateInternal(c[1]);
    }
}
