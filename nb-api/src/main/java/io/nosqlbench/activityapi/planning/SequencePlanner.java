/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.activityapi.planning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

public class SequencePlanner<T> {
    private final static Logger logger = LoggerFactory.getLogger(SequencePlanner.class);
    private SequencerType sequencerType;
    private List<T> elements = new ArrayList<>();
    private List<Long> ratios = new ArrayList<>();
    private int[] elementIndex;

    public SequencePlanner(SequencerType sequencerType) {
        this.sequencerType = sequencerType;
    }

    public void addOp(T elem, ToLongFunction<T> ratioFunc) {
        this.elements.add(elem);
        this.ratios.add(ratioFunc.applyAsLong(elem));
    }

    public void addOp(T elem, long func) {
        this.elements.add(elem);
        this.ratios.add(func);
    }

    public OpSequence<T> resolve() {
        switch (sequencerType) {
            case bucket:
                logger.trace("sequencing elements by simple round-robin");
                this.elementIndex = new BucketSequencer<T>().seqIndexesByRatios(elements, ratios);
                break;
            case interval:
                logger.trace("sequencing elements by interval and position");
                this.elementIndex = new IntervalSequencer<T>().seqIndexesByRatios(elements, ratios);
                break;
            case concat:
                logger.trace("sequencing elements by concatenation");
                this.elementIndex = new ConcatSequencer<T>().seqIndexesByRatios(elements, ratios);
        }
        this.elements = elements;
        return new Sequence<>(sequencerType, elements, elementIndex);
    }

    public static class Sequence<T> implements OpSequence<T> {
        private final SequencerType type;
        private final List<T> elems;
        private final int[] seq;

        Sequence(SequencerType type, List<T> elems, int[] seq) {
            this.type = type;
            this.elems = elems;
            this.seq = seq;
        }

        @Override
        public T get(long selector) {
            int index = (int) (selector % seq.length);
            index = seq[index];
            return elems.get(index);
        }

        @Override
        public List<T> getOps() {
            return elems;
        }

        @Override
        public int[] getSequence() {
            return seq;
        }

        public SequencerType getSequencerType() {
            return type;
        }

        @Override
        public <U> Sequence<U> transform(Function<T,U> func) {
            return new Sequence<U>(type, elems.stream().map(func).collect(Collectors.toList()), seq);
        }

    }

}
