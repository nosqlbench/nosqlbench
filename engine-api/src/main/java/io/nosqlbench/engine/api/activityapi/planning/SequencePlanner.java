/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.activityapi.planning;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToLongFunction;

public class SequencePlanner<T> {
    private final static Logger logger = LogManager.getLogger(SequencePlanner.class);
    private final SequencerType sequencerType;
    private final List<T> elements = new ArrayList<>();
    private final List<Long> ratios = new ArrayList<>();
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
        return new Sequence<>(sequencerType, elements, elementIndex);
    }

    public List<T> getElements() {
        return elements;
    }

    public List<Long> getRatios() {
        return ratios;
    }

}
