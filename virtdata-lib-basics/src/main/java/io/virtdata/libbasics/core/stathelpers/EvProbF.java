package io.virtdata.libbasics.core.stathelpers;

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
