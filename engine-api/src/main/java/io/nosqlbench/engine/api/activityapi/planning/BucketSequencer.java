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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

/**
 * <h2>Introduction</h2>
 * This class allows you to create a cyclic schedule that will maintain a mix
 * of elements according to individual ratios. This particular sequencer
 * simulates the effect of drawing from each bucket in round robin order until they
 * are empty, putting each event into the planned sequence in turn.
 *
 * While this is not a good way to evenly interleave events over a large number,
 * it is easy to reason about when looking at ratios like
 * A:5,B:3,C:1. In this case, the order of events would be A B C A B A B A A.
 #
 * <h2>Explanation and Example</h2>
 * <p>Given a set of three events A, B, and C, and a set of respective frequencies
 * of these events 5, 5, and 1. In short form: A:5,B:5,C:1. This means that a total
 * of 11 events will be scheduled. With this sequencing algorithm, source buckets of
 * events of type A, B, and C are initialized with the respective number of instances.
 * Then, each bucket in turn is drawn from in round robin fashion, with each bucket
 * being removed from the rotation when it becomes empty.
 *
 * <h2>Further Examples</h2>
 * <p>These examples simply show in symbolic terms how the ordering is affected by
 * different ratios.</p>
 * <ul>
 * <li>X:1,Y:1,Z:1 - X Y Z</li>
 * <li>L:4,M:1 - L M L L L</li>
 * <li>A:4,B:3,C:2,D:1 - A B C D A B C A B A</li>
 * <li>A:1,B:2:C:3:D:4 - A B C D B C D C D D</li>
 * <li>D:4,C:3,B:2,A:1 - D C B A D C B D C D</li>
 * </ul>
 *
 * @param <T> The element which is to be scheduled.
 */
public class BucketSequencer<T> implements ElementSequencer<T> {

    private List<T> elems;
    private ToLongFunction<T> ratioFunc;

    @Override
    public int[] seqIndexByRatioFunc(List<T> elems, ToLongFunction<T> ratioFunc) {

        List<Long> ratios = new ArrayList<>();
        for (int i = 0; i< elems.size(); i++) {
            T elem = elems.get(i);
            ratios.add(ratioFunc.applyAsLong(elem));
        }
        return seqIndexesByRatios(elems,ratios);
    }

    @Override
    public int[] seqIndexesByRatios(List<T> elems, List<Long> ratios) {
        List<OpBucket<T>> buckets = new ArrayList<>();
        List<Integer> sequence = new ArrayList<>();

        if (elems.size()!=ratios.size()) {
            throw new RuntimeException("Elements and Ratios must be pair-wise.");
        }

        for (int i = 0; i < elems.size(); i++) {
            T elem = elems.get(i);
            long ratio = ratios.get(i);
            if (ratio>0) {
                buckets.add(new OpBucket<>(elem,i,ratio));
            }
        }

        while(!buckets.isEmpty()) {
            buckets.forEach(b -> sequence.add(b.dispenseRank()));
            buckets = buckets.stream().filter(b -> b.count>0).collect(Collectors.toCollection(LinkedList::new));
        }
        return sequence.stream().mapToInt(i -> i).toArray();

    }

    private final static class OpBucket<T> {

        private final int rank;
        private final T elem;
        private long count;

        OpBucket(T elem, int rank, long ratio) {
            this.elem = elem;
            this.rank = rank;
            this.count = ratio;
        }

        public boolean isEmpty() {
            return (count==0);
        }

        int dispenseRank() {
            count--;
            return rank;
        }
    }


}
