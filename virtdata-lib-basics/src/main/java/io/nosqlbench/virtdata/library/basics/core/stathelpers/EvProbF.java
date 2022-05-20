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


/**
 * A simple wrapper type for "Event Probability", where the event is identified by a unique int,
 * and the probability is represented with single precision floating-point.
 */
public class EvProbF implements Comparable<EvProbF> {
    private int eventId;
    private float probability;

    public EvProbF(int eventId, float probability) {
        this.eventId = eventId;
        this.probability = probability;
    }

    public float getProbability() {
        return probability;
    }

    public int getEventId() {
        return eventId;
    }

    @Override
    public int compareTo(EvProbF other) {
        int diff = Float.compare(probability, other.getProbability());
        if (diff!=0) { return diff; }
        return Integer.compare(eventId, other.getEventId());
    }

    @Override
    public String toString() {
        return this.getEventId() + ":" + getProbability();
    }

}
