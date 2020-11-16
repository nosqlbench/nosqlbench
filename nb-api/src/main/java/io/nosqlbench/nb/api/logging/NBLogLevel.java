package io.nosqlbench.nb.api.logging;

public enum NBLogLevel {
    NONE(0L),
    FATAL(1L << 0),
    ERROR(1L << 1),
    WARN(1L << 2),
    INFO(1L << 3),
    DEBUG(1L << 4),
    TRACE(1L << 5),
    ALL(1L << 30),
    ;

    private final long level;

    NBLogLevel(long level) {
        this.level = level;
    }

    public static NBLogLevel valueOfName(String name) {
        for (NBLogLevel possible : NBLogLevel.values()) {
            if (name.toUpperCase().equals(possible.toString())) {
                return possible;
            }
        }
        throw new RuntimeException("Unable to find NBLogLevel for " + name);
    }

    public static NBLogLevel max(NBLogLevel... levels) {
        NBLogLevel max = NBLogLevel.NONE;
        for (NBLogLevel level : levels) {
            if (level.level > max.level) {
                max = level;
            }
        }
        return max;
    }

    public boolean isGreaterOrEqualTo(NBLogLevel other) {
        return level >= other.level;
    }
}
