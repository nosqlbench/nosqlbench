package io.nosqlbench.activitytype.cql.statements.rowoperators.verification;

public enum DiffType {

    /// Verify nothing for this statement
    none(0),

    /// Verify that fields named in the row are present in the reference map.
    rowfields(0x1),

    /// Verify that fields in the reference map are present in the row data.
    reffields(0x1 << 1),

    /// Verify that all fields present in either the row or the reference data
    /// are also present in the other.
    fields(0x1 | 0x1 << 1),

    /// Verify that all values of the same named field are equal, according to
    /// {@link Object#equals(Object)}}.
    values(0x1<<2),

    /// Cross-verify all fields and field values between the reference data and
    /// the actual data.
    all(0x1|0x1<<1|0x1<<2);

    public int bitmask;

    DiffType(int bit) {
        this.bitmask = bit;
    }

    public boolean is(DiffType option) {
        return (bitmask & option.bitmask) > 0;
    }

}
