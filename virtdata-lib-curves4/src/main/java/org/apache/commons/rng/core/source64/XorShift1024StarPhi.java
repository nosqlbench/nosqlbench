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

/**
 * A fast RNG implementing the {@code XorShift1024*} algorithm.
 *
 * <p>Note: This supersedes {@link XorShift1024Star}. The sequences emitted by both
 * generators are correlated.</p>
 *
 * <p>This generator differs only in the final multiplier (a fixed-point representation
 * of the golden ratio), which eliminates linear dependencies from one of the lowest
 * bits.</p>
 *
 * @see <a href="http://xorshift.di.unimi.it/xorshift1024star.c">Original source code</a>
 * @see <a href="https://en.wikipedia.org/wiki/Xorshift">Xorshift (Wikipedia)</a>
 * @since 1.3
 */
public class XorShift1024StarPhi extends XorShift1024Star {
    /**
     * Creates a new instance.
     *
     * @param seed Initial seed.
     * If the length is larger than 16, only the first 16 elements will
     * be used; if smaller, the remaining elements will be automatically
     * set. A seed containing all zeros will create a non-functional generator.
     */
    public XorShift1024StarPhi(long[] seed) {
        super(seed, 0x9e3779b97f4a7c13L);
    }
}
