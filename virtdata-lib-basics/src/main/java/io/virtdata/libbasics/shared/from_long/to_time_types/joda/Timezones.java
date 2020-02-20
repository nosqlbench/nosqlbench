package io.virtdata.libbasics.shared.from_long.to_time_types.joda;

import org.joda.time.DateTimeZone;

public class Timezones {
    public static DateTimeZone forId(String id) {
        try {
            return DateTimeZone.forID(id);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unable to find timezone for id '" + id + "'. Consider one of these:" +
                    DateTimeZone.getAvailableIDs());
        }
    }
}
