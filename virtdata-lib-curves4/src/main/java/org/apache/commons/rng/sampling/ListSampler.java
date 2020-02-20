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

package org.apache.commons.rng.sampling;

import org.apache.commons.rng.UniformRandomProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Sampling from a {@link List}.
 *
 * <p>This class also contains utilities for shuffling a {@link List} in-place.</p>
 *
 * @since 1.0
 */
public class ListSampler {
    /**
     * Class contains only static methods.
     */
    private ListSampler() {}

    /**
     * Generates a list of size {@code k} whose entries are selected
     * randomly, without repetition, from the items in the given
     * {@code collection}.
     *
     * <p>
     * Sampling is without replacement; but if the source collection
     * contains identical objects, the sample may include repeats.
     * </p>
     *
     * <p>
     * Sampling uses {@link UniformRandomProvider#nextInt(int)}.
     * </p>
     *
     * @param <T> Type of the list items.
     * @param rng Generator of uniformly distributed random numbers.
     * @param collection List to be sampled from.
     * @param k Size of the returned sample.
     * @throws IllegalArgumentException if {@code k <= 0} or
     * {@code k > collection.size()}.
     * @return a shuffled sample from the source collection.
     */
    public static <T> List<T> sample(UniformRandomProvider rng,
                                     List<T> collection,
                                     int k) {
        final int n = collection.size();
        final PermutationSampler p = new PermutationSampler(rng, n, k);
        final List<T> result = new ArrayList<T>(k);
        final int[] index = p.sample();

        for (int i = 0; i < k; i++) {
            result.add(collection.get(index[i]));
        }

        return result;
    }

    /**
     * Shuffles the entries of the given array.
     *
     * @see #shuffle(UniformRandomProvider,List,int,boolean)
     *
     * @param <T> Type of the list items.
     * @param rng Random number generator.
     * @param list List whose entries will be shuffled (in-place).
     */
    public static <T> void shuffle(UniformRandomProvider rng,
                                   List<T> list) {
        shuffle(rng, list, 0, false);
    }

    /**
     * Shuffles the entries of the given array, using the
     * <a href="http://en.wikipedia.org/wiki/Fisher-Yates_shuffle#The_modern_algorithm">
     * Fisher-Yates</a> algorithm.
     * The {@code start} and {@code pos} parameters select which part
     * of the array is randomized and which is left untouched.
     *
     * <p>
     * Sampling uses {@link UniformRandomProvider#nextInt(int)}.
     * </p>
     *
     * @param <T> Type of the list items.
     * @param rng Random number generator.
     * @param list List whose entries will be shuffled (in-place).
     * @param start Index at which shuffling begins.
     * @param towardHead Shuffling is performed for index positions between
     * {@code start} and either the end (if {@code false}) or the beginning
     * (if {@code true}) of the array.
     */
    public static <T> void shuffle(UniformRandomProvider rng,
                                   List<T> list,
                                   int start,
                                   boolean towardHead) {
        final int len = list.size();
        final int[] indices = PermutationSampler.natural(len);
        PermutationSampler.shuffle(rng, indices, start, towardHead);

        final ArrayList<T> items = new ArrayList<T>(list);
        for (int i = 0; i < len; i++) {
            list.set(i, items.get(indices[i]));
        }
    }
}
