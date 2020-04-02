/*
 *
 *       Copyright 2015 Jonathan Shook
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.function.LongFunction;

/**
 * This function wraps an epoch time in milliseconds into a String
 * as specified in the format. The valid formatters are documented
 * at @see <a href="https://www.joda.org/joda-time/apidocs/org/joda/time/format/DateTimeFormat.html">DateTimeFormat API Docs</a>
 */
@ThreadSafeMapper
@Categories({Category.datetime,Category.conversion})
public class StringDateWrapper implements LongFunction<String> {

    private DateTimeFormatter formatter;

    public StringDateWrapper(String format) {
        this.formatter = DateTimeFormat.forPattern(format);
    }

    @Override
    public String apply(long input) {
        String value = formatter.print(input);
        return value;
    }
}
