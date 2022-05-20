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


import java.nio.ByteBuffer;
import java.util.Iterator;

public class DiscreteProbabilityBuffer implements Iterable<DiscreteProbabilityBuffer.Entry> {

    private static int REFERENT_ID =0;
    private static int PROBABILITY = REFERENT_ID +Integer.BYTES;
    public static int RECORD_LEN = PROBABILITY +Double.BYTES;
    private double cumulativeProbability = 0.0D;
    private boolean isNormalized=false;

    private final ByteBuffer buffer;

    public DiscreteProbabilityBuffer(int entries) {
        this.buffer = ByteBuffer.allocate(entries*RECORD_LEN);
    }

    public DiscreteProbabilityBuffer add(int i, double probability) {
        cumulativeProbability+=probability;
        buffer.putInt(i);
        buffer.putDouble(probability);
        return this;
    }

    /**
     * Normalize the dataset, but only if the cumulative probability is not close to
     * the unit probability of 1.0D, within some phi threshold. In either case,
     * mark the dataset as normalized.
     * @param phi A double value, preferably very small, like 0.000000001D
     */
    public void normalize(double phi) {
        if (Math.abs(cumulativeProbability-1.0D)<phi) {
            isNormalized=true;
        } else {
            normalize();
        }

    }

    public void normalize() {
        if (isNormalized) {
            throw new RuntimeException("Attempt to re-normalize the data. Allowing this might decrease accuracy.");
        }
        buffer.flip();
        if ((buffer.capacity()% RECORD_LEN)!=0) {
            throw new RuntimeException("Buffer must be exactly a multiple of " + RECORD_LEN + " bytes. It is " + buffer.capacity());
        }
        int records = buffer.capacity() / RECORD_LEN;

        for (int i = 0; i < records; i++) {
            int doubleOffset = (i* RECORD_LEN) + PROBABILITY;
            double scalarProbability = buffer.getDouble(doubleOffset);
            double unitProbability = scalarProbability / cumulativeProbability;
            buffer.putDouble(doubleOffset,unitProbability);
        }
        cumulativeProbability=1.0D;
        isNormalized=true;
    }


    @Override
    public Iterator<Entry> iterator() {
        return new Iter(buffer.duplicate());
    }

    public double getCumulativeProbability() {
        return cumulativeProbability;
    }

    private static class Iter implements Iterator<Entry> {

        private ByteBuffer iterbuf;

        public Iter(ByteBuffer iterbuf) {
            this.iterbuf = iterbuf;
        }

        @Override
        public boolean hasNext() {
            return iterbuf.remaining()>0;
        }

        @Override
        public Entry next() {
            int eid = iterbuf.getInt();
            double prob = iterbuf.getDouble();
            return new Entry(eid,prob);
        }
    }

    public static class Entry {
        private int eventId;
        private double probability;

        public Entry(int eventId, double probability) {
            this.eventId = eventId;
            this.probability = probability;
        }

        public int getEventId() {
            return eventId;
        }

        public double getProbability() {
            return probability;
        }
    }

}
