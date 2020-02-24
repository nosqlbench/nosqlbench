package io.nosqlbench.virtdata.annotations;

public enum Range {
    NonNegativeLongs("All positive long values and zero: 0L.." + Long.MAX_VALUE),
    NonNegativeInts("All positive integer values and zero: 0.." + Integer.MAX_VALUE),
    Longs("All long values: " + Long.MIN_VALUE + "L.." + Long.MAX_VALUE+"L"),
    Integers("All int values: " + Integer.MIN_VALUE + ".." + Integer.MAX_VALUE),
    DoubleUnitInterval("The unit interval in double precision: 0.0D..1.0D"),
    FloatUnitInterval("The unit interval in single precision: 0.0F..1.0F"),
    Doubles("All double values: " + Double.MIN_VALUE + "D.." + Double.MAX_VALUE+"D");

    private final String description;

    public String getDescription() {
        return description;
    }

    Range(String description) {
        this.description = description;
    }
}
