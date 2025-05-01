/*
 * Copyright (c) nosqlbench
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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.stream.Collectors;

/**
 * Uses the alias sampling method to encode and sample from discrete probabilities,
 * even over larger sets of data. This form requires a unit interval sample value
 * between 0.0 and 1.0. Assuming the maximal amount of memory is used for distinct
 * outcomes N, a memory buffer of N*16 bytes is required for this implementation,
 * requiring 32MB of memory for 1M entries.
 *
 * This sampler should be shared between threads, and will be by default, in order
 * to avoid many instances of a 32MB buffer on heap.
 */
public class AliasSamplerDoubleLong implements DoubleToLongFunction {

    private final ByteBuffer stats; // tuples of double,int,int (unfair coin, direct pointers to referents)
    private final double slotCount; // The number of fair die-roll slotCount that contain unfair coin probabilities
    private static final int _r0=0;
    private static final int _r1=_r0+Double.BYTES; // unfair coin
    private static final int _r2=_r1+Long.BYTES; // + referent 1
    public static int RECORD_LEN = _r2 + Long.BYTES; // + referent 2 = Record size for the above.

    // for testing
    AliasSamplerDoubleLong(ByteBuffer stats) {
        this.stats = stats;
        if ((stats.capacity()% RECORD_LEN)!=0) {
            throw new RuntimeException("Misaligned ByteBuffer size, must be a multiple of " + RECORD_LEN);
        }
        slotCount = (stats.capacity()/ RECORD_LEN);
    }

    public AliasSamplerDoubleLong(List<EvProbLongDouble> events) {
        int size = events.size();

        int[] alias = new int[events.size()];
        double[] prob = new double[events.size()];

        LinkedList<EvProbLongDouble> small = new LinkedList<>();
        LinkedList<EvProbLongDouble> large = new LinkedList<>();
        List<Slot> slots = new ArrayList<>();

        // array-size normalization
        double sumProbability = events.stream().mapToDouble(EvProbLongDouble::prob).sum();
        events = events.stream().map(e -> new EvProbLongDouble(e.id(),
            (e.prob()/sumProbability)*size)).collect(Collectors.toList());

        // presort
        for (EvProbLongDouble event : events) {
            (event.prob()<1.0D ? small : large).addLast(event);
        }

        while (small.peekFirst()!=null && large.peekFirst()!=null) {
            EvProbLongDouble l = small.removeFirst();
            EvProbLongDouble g = large.removeFirst();
            slots.add(new Slot(g.id(), l.id(), l.prob()));
            EvProbLongDouble remainder = new EvProbLongDouble(g.id(),(g.prob()+l.prob())-1);
            (remainder.prob()<1.0D ? small : large).addLast(remainder);
        }
        while (large.peekFirst()!=null) {
            EvProbLongDouble g = large.removeFirst();
            slots.add(new Slot(g.id(),g.id(),1.0));
        }
        while (small.peekFirst()!=null) {
            EvProbLongDouble l = small.removeFirst();
            slots.add(new Slot(l.id(),l.id(),1.0));
        }
        if (slots.size()!=size) {
            throw new RuntimeException("basis for average probability is incorrect, because only " + slots.size() + " slotCount of " + size + " were created.");
        }
        // align to indexes
        for (int i = 0; i < slots.size(); i++) {
            slots.set(i,slots.get(i).rescale(i, i+1));
        }
        this.stats = ByteBuffer.allocate(slots.size()* RECORD_LEN);

        for (Slot slot : slots) {
            stats.putDouble(slot.botProb);
            stats.putLong(slot.botId());
            stats.putLong(slot.topId());
        }
        stats.flip();
        this.slotCount = (stats.capacity()/ RECORD_LEN);

    }

    @Override
    public long applyAsLong(double value) {
        double fractionlPoint = value * slotCount;
        int offsetPoint = (int) fractionlPoint * RECORD_LEN;
        double divider = stats.getDouble(offsetPoint);
        int selector = offsetPoint+ (fractionlPoint>divider?_r2:_r1);
        long referentId = stats.getLong(selector);
        return referentId;
    }

    private record Slot(long topId, long botId, double botProb){
        public Slot rescale(int min, int max) {
            return new Slot(topId, botId, (min + (botProb*(max-min))));
        }
    };
}
