package io.nosqlbench.nb.api.annotations;

public enum Span {
    /**
     * A span of time of size zero.
     */
    instant,
    /**
     * A span in time for which the start and end are different.
     */
    interval
}
