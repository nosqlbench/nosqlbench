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
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.internal.ProviderBuilder;
import org.apache.commons.rng.simple.internal.SeedFactory;

/**
 * This class provides the API for creating generators of random numbers.
 *
 * <p>Usage examples:</p>
 * <pre><code>
 *  UniformRandomProvider rng = RandomSource.create(RandomSource.MT);
 * </code></pre>
 * or
 * <pre><code>
 *  final int[] seed = new int[] { 196, 9, 0, 226 };
 *  UniformRandomProvider rng = RandomSource.create(RandomSource.MT, seed);
 * </code></pre>
 * or
 * <pre><code>
 *  final int[] seed = RandomSource.createIntArray(256);
 *  UniformRandomProvider rng = RandomSource.create(RandomSource.MT, seed);
 * </code></pre>
 * where the first argument to method {@code create} is the identifier
 * of the generator's concrete implementation, and the second the is the
 * (optional) seed.
 *
 * <p>
 * In the first form, a random seed will be {@link SeedFactory generated
 * automatically}; in the second form, a fixed seed is used; a random seed
 * is explicitly generated in the third form.
 * </p>
 *
 * <p>
 * Seeding is the procedure by which a value (or set of values) is
 * used to <i>initialize</i> a generator instance.
 * The requirement that a given seed will always result in the same
 * internal state allows to create different instances of a generator
 * that will produce the same sequence of pseudo-random numbers.
 * </p>
 *
 * <p>
 * The type of data used as a seed depends on the concrete implementation
 * as some types may not provide enough information to fully initialize
 * the generator's internal state.
 * <br>
 * The reference algorithm's seeding procedure (if provided) operates
 * on a value of a (single) <i>native</i> type:
 * Each concrete implementation's constructor creates an instance using
 * the native type whose information contents is used to set the
 * internal state.
 * <br>
 * When the seed value passed by the caller is of the native type, it is
 * expected that the sequences produced will be identical to those
 * produced by other implementations of the same reference algorithm.
 * <br>
 * However, when the seed value passed by the caller is not of the native
 * type, a transformation is performed by this library and the resulting
 * native type value will <i>not</i> contain more information than the
 * original seed value.
 * If the algorithm's native type is "simpler" than the type passed by
 * the caller, then some (unused) information will even be lost.
 * <br>
 * The transformation from non-native to native seed type is arbitrary,
 * as long as it does not reduce the amount of information required by
 * the algorithm to initialize its state.
 * The consequence of the transformation is that sequences produced
 * by this library may <i>not</i> be the same as the sequences produced
 * by other implementations of the same algorithm!
 * </p>
 *
 * <p>
 * For each algorithm, the Javadoc mentions the "ideal" size of the seed,
 * meaning the number of {@code int} or {@code long} values that is neither
 * too large (i.e. some of the seed is useless) or too small (i.e. an
 * internal procedure will fill the state with redundant information
 * computed from the given seed).
 * </p>
 *
 * <p>
 * Note that some algorithms are inherently sensitive to having too low
 * diversity in their initial state.
 * For example, it is often a bad idea to use a seed that is mostly
 * composed of zeroes, or of repeated values.
 * </p>
 *
 * <p>
 * This class provides methods to generate random seeds (single values
 * or arrays of values, of {@code int} or {@code long} types) that can
 * be passed to the {@link RandomSource#create(RandomSource,Object,Object[])
 * generators factory method}.
 * </p>
 * <p>
 * Although the seed-generating methods defined in this class will likely
 * return different values each time they are called, there is no guarantee:
 * </p>
 * <ul>
 *  <li>
 *   In any sub-sequence, it is <a href="https://en.wikipedia.org/wiki/Birthday_problem">
 *   expected</a> that the same numbers can occur, with a probability getting
 *   higher as the range of allowed values is smaller and the sequence becomes
 *   longer.
 *  </li>
 *  <li>
 *   It possible that the resulting "seed" will not be <i>good</i> (i.e.
 *   it will not generate a sufficiently uniformly random sequence for the
 *   intended purpose), even if the generator is good!
 *   The only way to ensure that the selected seed will make the generator
 *   produce a good sequence is to submit that sequence to a series of
 *   stringent tests, as provided by tools such as
 *   <a href="http://www.phy.duke.edu/~rgb/General/dieharder.php">dieharder</a>
 *   or <a href="http://simul.iro.umontreal.ca/testu01/tu01.html">TestU01</a>.
 *  </li>
 * </ul>
 *
 * <p>
 * The current implementations have no provision for producing non-overlapping
 * sequences.
 * For parallel applications, a possible workaround is that each thread uses
 * a generator of a different type (see {@link #TWO_CMRES_SELECT}).
 * </p>
 *
 * <p>
 * <b>Note:</b>
 * Seeding is not equivalent to restoring the internal state of an
 * <i>already initialized</i> generator.
 * Indeed, generators can have a state that is more complex than the
 * seed, and seeding is thus a transformation (from seed to state).
 * Implementations do not provide the inverse transformation (from
 * state to seed), hence it is not generally possible to know the seed
 * that would initialize a new generator instance to the current state
 * of another instance.
 * Reseeding is also inefficient if the purpose is to continue the
 * same sequence where another instance left off, as it would require
 * to "replay" all the calls performed by that other instance (and it
 * would require to know the number of calls to the primary source of
 * randomness, which is also not usually accessible).
 * </p>
 *
 * @since 1.0
 */
public enum RandomSource {
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source32.JDKRandom}.
     * <ul>
     *  <li>Native seed type: {@code Long}.</li>
     *  <li>Native seed size: 1.</li>
     * </ul>
     */
    JDK(ProviderBuilder.RandomSourceInternal.JDK),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source32.Well512a}.
     * <ul>
     *  <li>Native seed type: {@code int[]}.</li>
     *  <li>Native seed size: 16.</li>
     * </ul>
     */
    WELL_512_A(ProviderBuilder.RandomSourceInternal.WELL_512_A),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source32.Well1024a}.
     * <ul>
     *  <li>Native seed type: {@code int[]}.</li>
     *  <li>Native seed size: 32.</li>
     * </ul>
     */
    WELL_1024_A(ProviderBuilder.RandomSourceInternal.WELL_1024_A),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source32.Well19937a}.
     * <ul>
     *  <li>Native seed type: {@code int[]}.</li>
     *  <li>Native seed size: 624.</li>
     * </ul>
     */
    WELL_19937_A(ProviderBuilder.RandomSourceInternal.WELL_19937_A),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source32.Well19937c}.
     * <ul>
     *  <li>Native seed type: {@code int[]}.</li>
     *  <li>Native seed size: 624.</li>
     * </ul>
     */
    WELL_19937_C(ProviderBuilder.RandomSourceInternal.WELL_19937_C),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source32.Well44497a}.
     * <ul>
     *  <li>Native seed type: {@code int[]}.</li>
     *  <li>Native seed size: 1391.</li>
     * </ul>
     */
    WELL_44497_A(ProviderBuilder.RandomSourceInternal.WELL_44497_A),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source32.Well44497b}.
     * <ul>
     *  <li>Native seed type: {@code int[]}.</li>
     *  <li>Native seed size: 1391.</li>
     * </ul>
     */
    WELL_44497_B(ProviderBuilder.RandomSourceInternal.WELL_44497_B),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source32.MersenneTwister}.
     * <ul>
     *  <li>Native seed type: {@code int[]}.</li>
     *  <li>Native seed size: 624.</li>
     * </ul>
     */
    MT(ProviderBuilder.RandomSourceInternal.MT),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source32.ISAACRandom}.
     * <ul>
     *  <li>Native seed type: {@code int[]}.</li>
     *  <li>Native seed size: 256.</li>
     * </ul>
     */
    ISAAC(ProviderBuilder.RandomSourceInternal.ISAAC),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source64.SplitMix64}.
     * <ul>
     *  <li>Native seed type: {@code Long}.</li>
     *  <li>Native seed size: 1.</li>
     * </ul>
     */
    SPLIT_MIX_64(ProviderBuilder.RandomSourceInternal.SPLIT_MIX_64),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source64.XorShift1024Star}.
     * <ul>
     *  <li>Native seed type: {@code long[]}.</li>
     *  <li>Native seed size: 16.</li>
     * </ul>
     *
     * @deprecated Since 1.3, where it is recommended to use {@code XOR_SHIFT_1024_S_PHI}
     * instead due to its slightly better (more uniform) output. {@code XOR_SHIFT_1024_S}
     * is still quite usable but both are variants of the same algorithm and maintain their
     * internal state identically. Their outputs are correlated and the two should not be
     * used together when independent sequences are assumed.
     */
    @Deprecated
    XOR_SHIFT_1024_S(ProviderBuilder.RandomSourceInternal.XOR_SHIFT_1024_S),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source64.TwoCmres}.
     * This generator is equivalent to {@link #TWO_CMRES_SELECT} with the choice of the
     * pair {@code (0, 1)} for the two subcycle generators.
     * <ul>
     *  <li>Native seed type: {@code Integer}.</li>
     *  <li>Native seed size: 1.</li>
     * </ul>
     */
    TWO_CMRES(ProviderBuilder.RandomSourceInternal.TWO_CMRES),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source64.TwoCmres},
     * with explicit selection of the two subcycle generators.
     * The selection of the subcycle generator is by passing its index in the internal
     * table, a value between 0 (included) and 13 (included).
     * The two indices must be different.
     * Different choices of an ordered pair of indices create independent generators.
     * <ul>
     *  <li>Native seed type: {@code Integer}.</li>
     *  <li>Native seed size: 1.</li>
     * </ul>
     */
    TWO_CMRES_SELECT(ProviderBuilder.RandomSourceInternal.TWO_CMRES_SELECT),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source64.MersenneTwister64}.
     * <ul>
     *  <li>Native seed type: {@code long[]}.</li>
     *  <li>Native seed size: 312.</li>
     * </ul>
     */
    MT_64(ProviderBuilder.RandomSourceInternal.MT_64),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source32.MultiplyWithCarry256}.
     * <ul>
     *  <li>Native seed type: {@code int[]}.</li>
     *  <li>Native seed size: 257.</li>
     * </ul>
     */
    MWC_256(ProviderBuilder.RandomSourceInternal.MWC_256),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source32.KISSRandom}.
     * <ul>
     *  <li>Native seed type: {@code int[]}.</li>
     *  <li>Native seed size: 4.</li>
     * </ul>
     */
    KISS(ProviderBuilder.RandomSourceInternal.KISS),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source64.XorShift1024StarPhi}.
     * <ul>
     *  <li>Native seed type: {@code long[]}.</li>
     *  <li>Native seed size: 16.</li>
     * </ul>
     */
    XOR_SHIFT_1024_S_PHI(ProviderBuilder.RandomSourceInternal.XOR_SHIFT_1024_S_PHI),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source32.XoRoShiRo64Star}.
     * <ul>
     *  <li>Native seed type: {@code int[]}.</li>
     *  <li>Native seed size: 2.</li>
     * </ul>
     */
    XO_RO_SHI_RO_64_S(ProviderBuilder.RandomSourceInternal.XO_RO_SHI_RO_64_S),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source32.XoRoShiRo64StarStar}.
     * <ul>
     *  <li>Native seed type: {@code int[]}.</li>
     *  <li>Native seed size: 2.</li>
     * </ul>
     */
    XO_RO_SHI_RO_64_SS(ProviderBuilder.RandomSourceInternal.XO_RO_SHI_RO_64_SS),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source32.XoShiRo128Plus}.
     * <ul>
     *  <li>Native seed type: {@code int[]}.</li>
     *  <li>Native seed size: 4.</li>
     * </ul>
     */
    XO_SHI_RO_128_PLUS(ProviderBuilder.RandomSourceInternal.XO_SHI_RO_128_PLUS),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source32.XoShiRo128StarStar}.
     * <ul>
     *  <li>Native seed type: {@code int[]}.</li>
     *  <li>Native seed size: 4.</li>
     * </ul>
     */
    XO_SHI_RO_128_SS(ProviderBuilder.RandomSourceInternal.XO_SHI_RO_128_SS),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source64.XoRoShiRo128Plus}.
     * <ul>
     *  <li>Native seed type: {@code long[]}.</li>
     *  <li>Native seed size: 2.</li>
     * </ul>
     */
    XO_RO_SHI_RO_128_PLUS(ProviderBuilder.RandomSourceInternal.XO_RO_SHI_RO_128_PLUS),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source64.XoRoShiRo128StarStar}.
     * <ul>
     *  <li>Native seed type: {@code long[]}.</li>
     *  <li>Native seed size: 2.</li>
     * </ul>
     */
    XO_RO_SHI_RO_128_SS(ProviderBuilder.RandomSourceInternal.XO_RO_SHI_RO_128_SS),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source64.XoShiRo256Plus}.
     * <ul>
     *  <li>Native seed type: {@code long[]}.</li>
     *  <li>Native seed size: 4.</li>
     * </ul>
     */
    XO_SHI_RO_256_PLUS(ProviderBuilder.RandomSourceInternal.XO_SHI_RO_256_PLUS),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source64.XoShiRo256StarStar}.
     * <ul>
     *  <li>Native seed type: {@code long[]}.</li>
     *  <li>Native seed size: 4.</li>
     * </ul>
     */
    XO_SHI_RO_256_SS(ProviderBuilder.RandomSourceInternal.XO_SHI_RO_256_SS),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source64.XoShiRo512Plus}.
     * <ul>
     *  <li>Native seed type: {@code long[]}.</li>
     *  <li>Native seed size: 8.</li>
     * </ul>
     */
    XO_SHI_RO_512_PLUS(ProviderBuilder.RandomSourceInternal.XO_SHI_RO_512_PLUS),
    /**
     * Source of randomness is {@link org.apache.commons.rng.core.source64.XoShiRo512StarStar}.
     * <ul>
     *  <li>Native seed type: {@code long[]}.</li>
     *  <li>Native seed size: 8.</li>
     * </ul>
     */
    XO_SHI_RO_512_SS(ProviderBuilder.RandomSourceInternal.XO_SHI_RO_512_SS),
    ;

    /** Internal identifier. */
    private final ProviderBuilder.RandomSourceInternal internalIdentifier;

    /**
     * @param id Internal identifier.
     */
    RandomSource(ProviderBuilder.RandomSourceInternal id) {
        internalIdentifier = id;
    }

    /**
     * @return the internal identifier.
     */
    ProviderBuilder.RandomSourceInternal getInternalIdentifier() {
        return internalIdentifier;
    }

    /**
     * Checks whether the type of given {@code seed} is the native type
     * of the implementation.
     *
     * @param seed Seed value.
     * @return {@code true} if the type of {@code seed} is the native
     * type for this RNG source.
     */
    public boolean isNativeSeed(Object seed) {
        return internalIdentifier.isNativeSeed(seed);
    }

    /**
     * Creates a random number generator with a random seed.
     *
     * <p>Usage example:</p>
     * <pre><code>
     *  UniformRandomProvider rng = RandomSource.create(RandomSource.MT);
     * </code></pre>
     * <p>or, if a {@link RestorableUniformRandomProvider "save/restore"} functionality is needed,</p>
     * <pre><code>
     *  RestorableUniformRandomProvider rng = RandomSource.create(RandomSource.MT);
     * </code></pre>
     *
     * @param source RNG type.
     * @return the RNG.
     *
     * @see #create(RandomSource,Object,Object[])
     */
    public static RestorableUniformRandomProvider create(RandomSource source) {
        return create(source, null);
    }

    /**
     * Creates a random number generator with the given {@code seed}.
     *
     * <p>Usage example:</p>
     * <pre><code>
     *  UniformRandomProvider rng = RandomSource.create(RandomSource.TWO_CMRES_SELECT, 26219, 6, 9);
     * </code></pre>
     *
     * <p>Valid types for the {@code seed} are:</p>
     *  <ul>
     *   <li>{@code Integer} (or {@code int})</li>
     *   <li>{@code Long} (or {@code long})</li>
     *   <li>{@code int[]}</li>
     *   <li>{@code long[]}</li>
     *   <li>{@code byte[]}</li>
     *  </ul>
     *
     * <p>Notes:</p>
     * <ul>
     *  <li>
     *   When the seed type passed as argument is more complex (i.e. more
     *   bits can be independently chosen) than the generator's
     *   {@link #isNativeSeed(Object) native type}, the conversion of a
     *   set of different seeds will necessarily result in the same value
     *   of the native seed type.
     *  </li>
     *  <li>
     *   When the native seed type is an array, the same remark applies
     *   when the array contains more bits than the state of the generator.
     *  </li>
     *  <li>
     *   When the native seed type is an array and the {@code seed} is
     *   {@code null}, the size of the generated array will be 128.
     *  </li>
     * </ul>
     *
     * @param source RNG type.
     * @param seed Seed value.  It can be {@code null} (in which case a
     * random value will be used).
     * @param data Additional arguments to the implementation's constructor.
     * Please refer to the documentation of each specific implementation.
     * @return the RNG.
     * @throws UnsupportedOperationException if the type of the {@code seed}
     * is invalid.
     * @throws IllegalStateException if data is missing to initialize the
     * generator implemented by the given {@code source}.
     *
     * @see #create(RandomSource)
     */
    public static RestorableUniformRandomProvider create(RandomSource source,
                                                         Object seed,
                                                         Object ... data) {
        return ProviderBuilder.create(source.getInternalIdentifier(), seed, data);
    }

    /**
     * Creates a number for use as a seed.
     *
     * @return a random number.
     */
    public static int createInt() {
        return SeedFactory.createInt();
    }

    /**
     * Creates a number for use as a seed.
     *
     * @return a random number.
     */
    public static long createLong() {
        return SeedFactory.createLong();
    }

    /**
     * Creates an array of numbers for use as a seed.
     *
     * @param n Size of the array to create.
     * @return an array of {@code n} random numbers.
     */
    public static int[] createIntArray(int n) {
        return SeedFactory.createIntArray(n);
    }

    /**
     * Creates an array of numbers for use as a seed.
     *
     * @param n Size of the array to create.
     * @return an array of {@code n} random numbers.
     */
    public static long[] createLongArray(int n) {
        return SeedFactory.createLongArray(n);
    }

    /**
     * Wraps the given {@code delegate} generator in a new instance that
     * does not allow access to the "save/restore" functionality.
     *
     * @param delegate Generator to which calls will be delegated.
     * @return a new instance whose state cannot be saved or restored.
     */
    public static UniformRandomProvider unrestorable(final UniformRandomProvider delegate) {
        return new UniformRandomProvider() {
            /** {@inheritDoc} */
            @Override
            public void nextBytes(byte[] bytes) {
                delegate.nextBytes(bytes);
            }

            /** {@inheritDoc} */
            @Override
            public void nextBytes(byte[] bytes,
                                  int start,
                                  int len) {
                delegate.nextBytes(bytes, start, len);
            }

            /** {@inheritDoc} */
            @Override
            public int nextInt() {
                return delegate.nextInt();
            }

            /** {@inheritDoc} */
            @Override
            public int nextInt(int n) {
                return delegate.nextInt(n);
            }

            /** {@inheritDoc} */
            @Override
            public long nextLong() {
                return delegate.nextLong();
            }

            /** {@inheritDoc} */
            @Override
            public long nextLong(long n) {
                return delegate.nextLong(n);
            }

            /** {@inheritDoc} */
            @Override
            public boolean nextBoolean() {
                return delegate.nextBoolean();
            }

            /** {@inheritDoc} */
            @Override
            public float nextFloat() {
                return delegate.nextFloat();
            }

            /** {@inheritDoc} */
            @Override
            public double nextDouble() {
                return delegate.nextDouble();
            }

            /** {@inheritDoc} */
            @Override
            public String toString() {
                return delegate.toString();
            }
        };
    }
}
