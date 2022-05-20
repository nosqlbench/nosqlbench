package io.nosqlbench.virtdata.library.basics.shared.from_string.to_epoch;

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


import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.function.Function;

/**
 * This function will parse a String containing a formatted
 * date time, yielding a DateTime object.
 *
 * If no arguments are provided, then the format is set to
 * "yyyy-MM-dd HH:mm:ss.SSSZ".
 *
 * For details on formatting options, see @see <a href="https://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html">DateTimeFormat</a>
 */
@Categories(Category.datetime)
@ThreadSafeMapper
public class DateTimeParser implements Function<String, DateTime> {

    private final DateTimeFormatter dateTimeFormatter;
    private final DateTime defaultTime;

    /**
     * Initialize the parser with the default pattern of <pre>yyyy-MM-dd HH:mm:ss.SSSZ</pre>.
     */
    @Example({"DateTimeParser()","parse any date in the yyyy-MM-dd HH:mm:ss.SSSZ format"})
    public DateTimeParser() {
        this("yyyy-MM-dd HH:mm:ss.SSSZ", null);
    }

    /**
     * Initialize the parser with the given pattern. With this form, if any input fails to parse,
     * or is null or empty, then an exception is thrown.
     * @param dateTimePattern The pattern which represents the incoming format.
     */
    @Example({"DateTimeParser('yyyy-MM-dd')","parse any date in the yyyy-MM-dd format"})
    public DateTimeParser(String dateTimePattern) {
        this(dateTimePattern,null);
    }

    /**
     * Initialize the parser with the given pattern and default value. In this form, if any
     * input fails to parse, then exceptions are suppressed and the default is provided instead.
     * At initialization, the default is parsed as a sanity check.
     * @param dateTimePattern The pattern which represents the incoming format.
     * @param defaultTime An example of a formatted datetime string which is used as a default.
     */
    @Example({"DateTimeParser('yyyy-MM-dd','1999-12-31')","parse any date in the yyyy-MM-dd format, or return the DateTime represented by 1999-12-31"})
    public DateTimeParser(String dateTimePattern, String defaultTime) {
        this.dateTimeFormatter = DateTimeFormat.forPattern(dateTimePattern)
                .withChronology(GregorianChronology.getInstance());
        if (defaultTime != null) {
            try {
                this.defaultTime = dateTimeFormatter.parseDateTime(defaultTime);
            } catch (Exception e) {
                throw new RuntimeException("DateTimeParser with pattern '" + dateTimePattern + "' did " +
                        " not validate with default provided: '" + defaultTime + "'");

            }
        } else {
            this.defaultTime = null;
        }
    }

    @Override
    public DateTime apply(String formattedDateTime) {
        try {
            DateTime dateTime = dateTimeFormatter.parseDateTime(formattedDateTime);
            return dateTime;
        } catch (Exception e) {
            if (this.defaultTime != null) {
                return defaultTime;
            } else {
                throw new RuntimeException("Failed to parse '" + formattedDateTime + "' with '" + dateTimeFormatter + "'");
            }
        }

    }
}
