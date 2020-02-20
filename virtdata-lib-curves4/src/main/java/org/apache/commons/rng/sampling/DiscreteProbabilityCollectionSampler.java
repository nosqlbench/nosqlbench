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

import java.util.*;

/**
 * Sampling from a collection of items with user-defined
 * <a href="http://en.wikipedia.org/wiki/Probability_distribution#Discrete_probability_distribution">
 * probabilities</a>.
 * Note that if all unique items are assigned the same probability,
 * it is much more efficient to use {@link CollectionSampler}.
 *
 * <p>Sampling uses {@link UniformRandomProvider#nextDouble()}.</p>
 *
 * @param <T> Type of items in the collection.
 *
 * @since 1.1
 */
public class DiscreteProbabilityCollectionSampler<T> {
    /** Collection to be sampled from. */
    private final List<T> items;
    /** RNG. */
    private final UniformRandomProvider rng;
    /** Cumulative probabilities. */
    private final double[] cumulativeProbabilities;

    /**
     * Creates a sampler.
     *
     * @param rng Generator of uniformly distributed random numbers.
     * @param collection Collection to be sampled, with the probabilities
     * associated to each of its items.
     * A (shallow) copy of the items will be stored in the created instance.
     * The probabilities must be non-negative, but zero values are allowed
     * and their sum does not have to equal one (input will be normalized
     * to make the probabilities sum to one).
     * @throws IllegalArgumentException if {@code collection} is empty, a
     * probability is negative, infinite or {@code NaN}, or the sum of all
     * probabilities is not strictly positive.
     */
    public DiscreteProbabilityCollectionSampler(UniformRandomProvider rng,
                                                Map<T, Double> collection) {
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("Empty collection");
        }

        this.rng = rng;
        final int size = collection.size();
        items = new ArrayList<T>(size);
        cumulativeProbabilities = new double[size];

        double sumProb = 0;
        int count = 0;
        for (Map.Entry<T, Double> e : collection.entrySet()) {
            items.add(e.getKey());

            final double prob = e.getValue();
            if (prob < 0 ||
                Double.isInfinite(prob) ||
                Double.isNaN(prob)) {
                throw new IllegalArgumentException("Invalid probability: " +
                                                   prob);
            }

            // Temporarily store probability.
            cumulativeProbabilities[count++] = prob;
            sumProb += prob;
        }

        if (!(sumProb > 0)) {
            throw new IllegalArgumentException("Invalid sum of probabilities");
        }

        // Compute and store cumulative probability.
        for (int i = 0; i < size; i++) {
            cumulativeProbabilities[i] /= sumProb;
            if (i > 0) {
                cumulativeProbabilities[i] += cumulativeProbabilities[i - 1];
            }
        }
    }

    /**
     * Creates a sampler.
     *
     * @param rng Generator of uniformly distributed random numbers.
     * @param collection Collection to be sampled.
     * A (shallow) copy of the items will be stored in the created instance.
     * @param probabilities Probability associated to each item of the
     * {@code collection}.
     * The probabilities must be non-negative, but zero values are allowed
     * and their sum does not have to equal one (input will be normalized
     * to make the probabilities sum to one).
     * @throws IllegalArgumentException if {@code collection} is empty or
     * a probability is negative, infinite or {@code NaN}, or if the number
     * of items in the {@code collection} is not equal to the number of
     * provided {@code probabilities}.
     */
    public DiscreteProbabilityCollectionSampler(UniformRandomProvider rng,
                                                List<T> collection,
                                                double[] probabilities) {
        this(rng, consolidate(collection, probabilities));
    }

    /**
     * Picks one of the items from the collection passed to the constructor.
     *
     * @return a random sample.
     */
    public T sample() {
        final double rand = rng.nextDouble();

        int index = Arrays.binarySearch(cumulativeProbabilities, rand);
        if (index < 0) {
            index = -index - 1;
        }

        if (index < cumulativeProbabilities.length &&
            rand < cumulativeProbabilities[index]) {
            return items.get(index);
        }

        // This should never happen, but it ensures we will return a correct
        // object in case there is some floating point inequality problem
        // wrt the cumulative probabilities.
        return items.get(items.size() - 1);
    }

    /**
     * @param collection Collection to be sampled.
     * @param probabilities Probability associated to each item of the
     * {@code collection}.
     * @return a consolidated map (where probabilities of equal items
     * have been summed).
     * @throws IllegalArgumentException if the number of items in the
     * {@code collection} is not equal to the number of provided
     * {@code probabilities}.
     * @param <T> Type of items in the collection.
     */
    private static <T> Map<T, Double> consolidate(List<T> collection,
                                                  double[] probabilities) {
        final int len = probabilities.length;
        if (len != collection.size()) {
            throw new IllegalArgumentException("Size mismatch: " +
                                               len + " != " +
                                               collection.size());
        }

        final Map<T, Double> map = new HashMap<T, Double>();
        for (int i = 0; i < len; i++) {
            final T item = collection.get(i);
            final Double prob = probabilities[i];

            Double currentProb = map.get(item);
            if (currentProb == null) {
                currentProb = 0d;
            }

            map.put(item, currentProb + prob);
        }

        return map;
    }
}
