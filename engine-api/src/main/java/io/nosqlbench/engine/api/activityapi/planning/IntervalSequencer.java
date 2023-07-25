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
import java.util.Comparator;
import java.util.List;
import java.util.function.ToLongFunction;

/**
 * <h2>Introduction</h2>
 * This class allows you to create a cyclic schedule that will maintain a mix
 * of elements according to individual ratios. It is quite flexible and consistent
 * in how the scheduling works. This scheme requires a whole-numbered ratio for
 * each element, and produces a discrete schedule that indexes into the original
 * elements.
 *
 * Given a set of elements
 * and respective whole-numbered ratios of the frequency that they should occur,
 * IntervalSequencer creates an execution sequence that honors each element's expected
 * frequency within the cycle while interleaving the elements in a fair and stable way.
 *
 * <h2>Explanation and Example</h2>
 * <p>Given a set of three events A, B, and C, and a set of respective frequencies
 * of these events 5, 5, and 1. In short form: A:5,B:5,C:1. This means that a total
 * of 11 events will be scheduled. In order to support interleaving the events over
 * the cycle, each event is scheduled independently according to an ideal schedule.
 * For A, over the unit interval of (0,0,1.0),
 * it would occur at 0.0, 0.2, 0.4, 0.6, and 0.8, restarting again at the next unit
 * interval. B would have exactly the same ideal schedule. However, A occurs first
 * in the list, and is given sequencing precedence at any point at which both events
 * occur at exactly the same scheduled offset. Essentially, events are first scheduled
 * in the unit interval according to their ideal equidistant schedule in the unit interval,
 * and then by their order of appearance as presented. The unit interval does not actually
 * determine when in time an event may occur, but is used for the major-minor sequencing
 * rules above. The result is merely a stable sequence for execution planning separate from
 * the specific timing of each event.
 *
 * <h2>Further Examples</h2>
 * <p>These examples simply show in symbolic terms how the ordering is affected by
 * different ratios. It is important to keep in mind that the ratios have no primary influence
 * on relative ordering when events overlap in time. In that case, order of presentation
 * breaks the tie.</p>
 * <ul>
 * <li>X:1,Y:1,Z:1 - X Y Z</li>
 * <li>O:4,I:3 - O I O I O I O</li>
 * <li>O:4,I:4 - O I O I O I O I</li>
 * <li>O:4,I:5 - O I I O I O I O I</li>
 * </ul>
 *
 * @param <T> The element which is to be scheduled.
 */
public class IntervalSequencer<T> implements ElementSequencer<T> {

    @Override
    public int[] seqIndexByRatioFunc(List<T> elements, ToLongFunction<T> ratioFunc) {

        List<Long> ratios = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++) {
            T elem = elements.get(i);
            ratios.add(ratioFunc.applyAsLong(elem));
        }
        return seqIndexesByRatios(elements, ratios);
    }

    @Override
    public int[] seqIndexesByRatios(List<T> elems, List<Long> ratios) {
        List<IntervalSequencer.OpSlot<T>> ops = new ArrayList<>();

        for (int i = 0; i < elems.size(); i++) {
            T elem = elems.get(i);
            long freq = ratios.get(i);
            for (int p = 0; p < freq; p++) {
                double pos = (double) p / (double) freq;
                ops.add(new IntervalSequencer.OpSlot<>(elem, pos, i));
            }
        }

        ops.sort(new IntervalSequencer.OpComparator());

        return ops.stream().mapToInt(IntervalSequencer.OpSlot::getElementRank).toArray();
    }

    private final static class OpSlot<T> {

        private final T elem;
        private final double position;
        private final int rank;

        public OpSlot(T elem, double position, int rank) {
            this.elem = elem;
            this.position = position;
            this.rank = rank;
        }

        public T getElement() {
            return elem;
        }

        public int getElementRank() {
            return rank;
        }
    }


    private final static class OpComparator implements Comparator<OpSlot> {

        @Override
        public int compare(OpSlot o1, OpSlot o2) {
            int timeOrder = Double.compare(o1.position, o2.position);
            if (timeOrder != 0) {
                return timeOrder;
            }
            return Integer.compare(o1.rank, o2.rank);
        }
    }

}
