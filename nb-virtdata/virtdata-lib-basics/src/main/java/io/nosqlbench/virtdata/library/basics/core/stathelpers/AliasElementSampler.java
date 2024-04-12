/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.virtdata.library.basics.core.stathelpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Uses the alias sampling method to encode and sample from discrete probabilities,
 * even over large sets of data. This form requires a unit interval sample value
 * between 0.0 and 1.0. Assuming the maximal amount of memory is used for distinct
 * outcomes N, a memory buffer of N*16 bytes is required for this implementation,
 * requiring 32MB of memory for 1M entries.
 *
 * This sampler should be shared between threads, and will be by default, in order
 * to avoid many instances of a 32MB buffer on heap.
 */
public class AliasElementSampler<T> implements DoubleFunction<T> {

    private final double[] biases;
    private final T[] elements;
    private double slotCount; // The number of fair die-roll slotCount that contain unfair coin probabilities


    /**
     * Setup an alias table for T type objects.
     * @param biases An array of the unfair die model values
     * @param elements An array of elements of type T, two values per bias value. index 2n is bot, index 2n+1 is top.
     */
    AliasElementSampler(double[] biases, T[] elements) {
        this.biases = biases;
        this.elements = elements;
    }

    AliasElementSampler(Collection<T> elements, Function<T,Double> weightFunction) {
        this(elements.stream().map(e -> new ElemProbD<>(e,weightFunction.apply(e))).collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    public AliasElementSampler(List<ElemProbD<T>> events) {
        int size = events.size();

        LinkedList<ElemProbD<T>> small = new LinkedList<>();
        LinkedList<ElemProbD<T>> large = new LinkedList<>();
        List<Slot<T>> slots = new ArrayList<>();

        // array-size normalization
        double sumProbability = events.stream().mapToDouble(ElemProbD::getProbability).sum();
        events = events.stream().map(
                e -> new ElemProbD<>(e.getElement(), (e.getProbability() / sumProbability) * size)
        ).collect(Collectors.toList());

        // presort
        for (ElemProbD<T> event : events) {
            (event.getProbability()<1.0D ? small : large).addLast(event);
        }

        while (small.peekFirst()!=null && large.peekFirst()!=null) {
            ElemProbD<T> l = small.removeFirst();
            ElemProbD<T> g = large.removeFirst();
            slots.add(new Slot<>(g.getElement(), l.getElement(), l.getProbability()));
            g.setProbability((g.getProbability()+l.getProbability())-1);
            (g.getProbability()<1.0D ? small : large).addLast(g); // requeue
        }
        while (large.peekFirst()!=null) {
            ElemProbD<T> g = large.removeFirst();
            slots.add(new Slot<>(g.getElement(),g.getElement(),1.0));
        }
        while (small.peekFirst()!=null) {
            ElemProbD<T> l = small.removeFirst();
            slots.add(new Slot<>(l.getElement(),l.getElement(),1.0));
        }
        if (slots.size()!=size) {
            throw new RuntimeException("basis for average probability is incorrect, because only " + slots.size() + " slotCount of " + size + " were created.");
        }
        // align to indexes
        for (int i = 0; i < slots.size(); i++) {
            slots.get(i).rescale(i, i+1);
        }
        this.biases=new double[slots.size()];

        //noinspection unchecked
        elements = (T[]) new Object[biases.length*2];

        for (int i = 0; i < biases.length; i++) {
            biases[i]=slots.get(i).botProb;
            elements[i*2] = slots.get(i).botItx;
            elements[(i*2)+1] = slots.get(i).topIdx;
        }
        this.slotCount = biases.length;

    }

    @Override
    public T apply(double value) {
        double fractionlPoint = value * slotCount;
        int offsetPoint = (int) fractionlPoint;
        double divider = biases[offsetPoint];
        int index = fractionlPoint>divider? (offsetPoint<<1)+1 : (offsetPoint<<1);
        T element = elements[index];
        return element;
    }

    private static class Slot<T> {
        public T topIdx;
        public T botItx;
        public double botProb;

        public Slot(T topIdx, T botItx, double botProb) {
            this.topIdx = topIdx;
            this.botItx = botItx;
            this.botProb = botProb;
        }

        public String toString() {
            return "top:" + topIdx + ", bot:" + botItx + ", botProb: " + botProb;
        }

        public Slot rescale(double min, double max) {
            botProb = (min + (botProb*(max-min)));
            return this;
        }
    }

    public interface Weighted {
        double getWeight();
    }
}
