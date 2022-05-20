package io.nosqlbench.virtdata.library.basics.shared.from_long.to_time_types.joda;

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
