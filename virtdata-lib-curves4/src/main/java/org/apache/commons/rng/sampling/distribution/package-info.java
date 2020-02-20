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

/**
 * <h3>Distribution samplers</h3>
 *
 * <p>
 * This package contains classes for sampling from statistical distributions.
 * </p>
 *
 * <p>As of version 1.0, the code for specific distributions was adapted from
 * the corresponding classes in the development version of "Commons Math" (in
 * package {@code org.apache.commons.math4.distribution}).
 * </p>
 * <p>
 * When no specific algorithm is provided, one can still sample from any
 * distribution, using the <em>inverse method</em>, as illustrated in:
 * <ul>
 *  <li>{@link org.apache.commons.rng.sampling.distribution.InverseTransformDiscreteSampler InverseTransformDiscreteSampler}</li>
 *  <li>{@link org.apache.commons.rng.sampling.distribution.InverseTransformContinuousSampler InverseTransformContinuousSampler}</li>
 * </ul>
 *
 * Algorithms are described in e.g. Luc Devroye (1986), <a href="http://luc.devroye.org/chapter_nine.pdf">chapter 9</a>
 * and <a href="http://luc.devroye.org/chapter_ten.pdf">chapter 10</a>.
 *
 * This <a href="http://www.doc.ic.ac.uk/~wl/papers/07/csur07dt.pdf">paper</a> discusses Gaussian generators.
 */

package org.apache.commons.rng.sampling.distribution;
