package io.nosqlbench.virtdata.library.basics.core.stathelpers;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.util.Comparator;

/**
 * A simple wrapper type for "Event Probability", where the event is identified by a unique int,
 * and the probability is represented with double precision floating-point.
 */
public class ElemProbD<T> implements Comparable<ElemProbD<T>> {
    private final T element;
    private double probability;

    public ElemProbD(T element, double probability) {
        this.element = element;
        this.probability = probability;
    }

    public double getProbability() {
        return probability;
    }

    public T getElement() {
        return element;
    }

    @Override
    public int compareTo(ElemProbD other) {
        return Double.compare(probability, other.getProbability());
    }

    public static Comparator<ElemProbD> DESCENDING_PROBABILTY = (o1, o2) -> Double.compare(o2.probability,o1.probability);

    public void setProbability(double probability) {
        this.probability = probability;
    }

    @Override
    public String toString() {
        return (element==null ? "NULL" : element.toString()) + ":" + getProbability();
    }
}
