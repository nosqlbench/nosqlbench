package io.nosqlbench.virtdata.library.basics.core.stathelpers;

import java.util.Comparator;

/**
 * A simple wrapper type for "Event Probability", where the event is identified by a unique int,
 * and the probability is represented with double precision floating-point.
 */
public class EvProbD implements Comparable<EvProbD> {
    private int eventId;
    private double probability;

    public EvProbD(int eventId, double probability) {
        this.eventId = eventId;
        this.probability = probability;
    }

    public double getProbability() {
        return probability;
    }

    public int getEventId() {
        return eventId;
    }

    @Override
    public int compareTo(EvProbD other) {
        int diff = Double.compare(probability, other.getProbability());
        if (diff!=0) { return diff; }
        return Integer.compare(eventId, other.getEventId());
    }

    public static Comparator<EvProbD> DESCENDING_PROBABILTY =
            (Comparator<EvProbD>) (o1, o2) -> Double.compare(o2.probability,o1.probability);

    public void setProbability(double probability) {
        this.probability = probability;
    }

    @Override
    public String toString() {
        return this.getEventId() + ":" + getProbability();
    }
}
