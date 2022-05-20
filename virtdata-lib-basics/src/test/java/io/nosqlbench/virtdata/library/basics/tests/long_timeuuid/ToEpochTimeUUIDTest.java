package io.nosqlbench.virtdata.library.basics.tests.long_timeuuid;

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

        for (String formatted : formats) {
            System.out.println(formatted);
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
