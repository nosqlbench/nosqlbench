package io.nosqlbench.virtdata.library.basics.tests.long_timeuuid;

import io.nosqlbench.virtdata.library.basics.core.DateTimeFormats;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_time_types.ToEpochTimeUUID;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ToEpochTimeUUIDTest {

    @Test
    public void testFormats() {
        ToEpochTimeUUID e;

        List<String> formats = new ArrayList<>();
        for (DateTimeFormatter formatter : DateTimeFormats.formatters) {
            DateTime now = DateTime.now();
            String formatted = formatter.print(now);
            formats.add(formatted);

            e = new ToEpochTimeUUID(formatted);

        }

        /**
         2017-06-07 21:39:24.710-0500
         2017-06-07 21:39:24
         20170607T213924.711-0500
         20170608T023924Z
         2017-06-08
         20170608
         201706
         2017
         */

    }

}