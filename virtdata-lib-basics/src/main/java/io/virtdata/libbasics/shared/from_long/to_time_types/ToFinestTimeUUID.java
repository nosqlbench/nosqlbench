package io.virtdata.libbasics.shared.from_long.to_time_types;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.libbasics.core.DateTimeFormats;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.UUID;
import java.util.function.LongFunction;

/**
 * Converts a count of 100ns intervals from 1582 Julian to a Type1 TimeUUID
 * according to <a href="https://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
 * This allows you to access the finest unit of resolution for the
 * purposes of simulating a large set of unique timeuuid values. This offers
 * 10000 times more unique values per ms than {@link ToEpochTimeUUID}.
 *
 * For the variants that have a String argument in the constructor, this is
 * a parsable datetime that is used as the base time for all produced values.
 * Setting this allows you to set the start of the time range for all timeuuid
 * values produced. All times are parsed for UTC. All time use ISO date ordering,
 * meaning that the most significant fields always go before the others.
 *
 * The valid formats, in joda specifier form are:
 *
 * <ol>
 * <li>yyyy-MM-dd HH:mm:ss.SSSZ, for example: 2015-02-28 23:30:15.223</li>
 * <li>yyyy-MM-dd HH:mm:ss, for example 2015-02-28 23:30:15</li>
 * <li>yyyyMMdd'T'HHmmss.SSSZ, for example: 20150228T233015.223</li>
 * <li>yyyyMMdd'T'HHmmssZ, for example: 20150228T233015</li>
 * <li>yyyy-MM-dd, for example: 2015-02-28</li>
 * <li>yyyyMMdd, for example: 20150228</li>
 * <li>yyyyMM, for example: 201502</li>
 * <li>yyyy, for example: 2015</li>
 * </ol>
 */
@ThreadSafeMapper
@Categories({Category.datetime})
public class ToFinestTimeUUID implements LongFunction<UUID> {

    private final long node;
    private final long clock;
    private final long baseTicks;

    /**
     * Create version 1 timeuuids with a per-host node and empty clock data.
     * The node and clock components are seeded from network interface data. In this case,
     * the clock data is not seeded uniquely.
     */
    @Example({"ToFinestTimeUUID()","basetime 0, computed node data, empty clock data"})
    public ToFinestTimeUUID() {
        this.node = 0L;
        this.clock = 0L;
        this.baseTicks = 0L;
    }

    /**
     * Create version 1 timeuuids with a specific static node and empty clock data.
     * This is useful for testing so that you can know that values are verifiable, even though
     * in non-testing practice, you would rely on some form of entropy per-system to provide
     * more practical dispersion of values over reboots, etc.
     *
     * @param node a fixture value for testing that replaces node and clock bits
     */
    @Example({"ToFinestTimeUUID(5234)","basetime 0, specified node data (5234), empty clock data"})
    public ToFinestTimeUUID(long node) {
        this.node = node;
        this.clock = 0L;
        this.baseTicks = 0L;
    }

    /**
     * Create version 1 timeuuids with a specific static node and specific clock data.
     * This is useful for testing so that you can know that values are verifiable, even though
     * in non-testing practice, you would rely on some form of entropy per-system to provide
     * more practical dispersion of values over reboots, etc.
     *
     * @param node  a fixture value for testing that replaces node bits
     * @param clock a fixture value for testing that replaces clock bits
     */
    @Example({"ToFinestTimeUUID(31,337)","basetime 0, specified node data (31) and clock data (337)"})
    public ToFinestTimeUUID(long node, long clock) {
        this.node = node;
        this.clock = clock;
        this.baseTicks = 0L;
    }

    /**
     * Create version 1 timeuuids with a per-host node and empty clock data.
     * The node and clock components are seeded from network interface data. In this case,
     * the clock data is not seeded uniquely.
     *
     * @param baseTimeSpec - a string specification for the base time value
     */
    @Example({"ToFinestTimeUUID('2017-01-01T23:59:59')","specified basetime, computed node data, empty clock data"})
    public ToFinestTimeUUID(String baseTimeSpec) {
        this.node = 0L;
        this.clock = 0L;
        this.baseTicks = DateTimeFormats.parseEpochTimeToTimeUUIDTicks(baseTimeSpec);
    }

    /**
     * Create version 1 timeuuids with a specific static node and empty clock data.
     * This is useful for testing so that you can know that values are verifiable, even though
     * in non-testing practice, you would rely on some form of entropy per-system to provide
     * more practical dispersion of values over reboots, etc.
     *
     * @param baseTimeSpec - a string specification for the base time value
     * @param node         a fixture value for testing that replaces node and clock bits
     */
    @Example({"ToFinestTimeUUID('2012',12345)","basetime at start if 2012, with node data 12345, empty clock data"})
    public ToFinestTimeUUID(String baseTimeSpec, long node) {
        this.node = node;
        this.clock = 0L;
        this.baseTicks = DateTimeFormats.parseEpochTimeToTimeUUIDTicks(baseTimeSpec);
    }

    /**
     * Create version 1 timeuuids with a specific static node and specific clock data.
     * This is useful for testing so that you can know that values are verifiable, even though
     * in non-testing practice, you would rely on some form of entropy per-system to provide
     * more practical dispersion of values over reboots, etc.
     *
     * @param node         a fixture value for testing that replaces node bits
     * @param clock        a fixture value for testing that replaces clock bits
     * @param baseTimeSpec - a string specification for the base time value
     */
    @Example({"ToFinestTimeUUID('20171231T1015.243',123,456)","ms basetime, specified node and clock data"})
    public ToFinestTimeUUID(String baseTimeSpec, long node, long clock) {
        this.node = node;
        this.clock = clock;
        this.baseTicks = DateTimeFormats.parseEpochTimeToTimeUUIDTicks(baseTimeSpec);
    }

    private static long msbBitsFor(long timeClicks) {
        return 0x0000000000001000L
                | (0x0fff000000000000L & timeClicks) >>> 48
                | (0x0000ffff00000000L & timeClicks) >>> 16
                | (0x00000000ffffffffL & timeClicks) << 32;
    }

    private static long lsbBitsFor(long node, long clock) {
        return ((clock & 0x0000000000003FFFL) << 48) | 0x8000000000000000L | node;
    }

    @Override
    public UUID apply(long timeClicks) {
        return new UUID(msbBitsFor(timeClicks + baseTicks), lsbBitsFor(node, clock));
    }

}
