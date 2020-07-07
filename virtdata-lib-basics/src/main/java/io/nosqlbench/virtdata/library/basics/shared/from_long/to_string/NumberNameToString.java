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

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import uk.ydubey.formatter.numtoword.NumberInWordsFormatter;

import java.util.function.LongFunction;

/**
 * Provides the spelled-out name of a number. For example,
 * an input of 7 would yield "seven". An input of 4234
 * yields the value "four thousand thirty four".
 * The maximum value is limited at 999,999,999.
 */
@ThreadSafeMapper
public class NumberNameToString implements LongFunction<String> {

    private final static ThreadLocal<StringBuilder> tlsb = ThreadLocal.withInitial(StringBuilder::new);

    private final static String THOUSAND = "thousand";
    private final static String MILLION = "million";
    private final static String BILLION = "billion";
    private final static String TRILLION = "trillion";
    private final static String QUADRILLION = "quadrillion";
    private final static String QUINTILLION = "quintillion";

    private final NumberInWordsFormatter formatter = NumberInWordsFormatter.getInstance();

    @Override
    public String apply(long input) {
        if (input == 0L) {
            return "zero";
        } else if (input > 0 && input <= 999999999L) {
            return formatter.format((int) input);
        } else if (input < 0 && input >= -999999999L) {
            return "negative " + formatter.format((int) -input);
        } else {
            StringBuilder sb = tlsb.get();
            sb.setLength(0);
            long value = input;

            if (value < 0) {
                value = -value;
                sb.append("negative");
            }

            long higher = (value / 1000000000L);
            if (higher > 1000000000L) {
                long evenhigher = higher / 1000000000L;
                sb.append(formatter.format((int) evenhigher)).append(" quintillion");
                higher = higher % 1000000000L;
            }

            String val = formatter.format((int) higher)
                .replaceAll(THOUSAND, TRILLION)
                .replaceAll(MILLION, QUADRILLION)
                .replaceAll(BILLION, QUINTILLION);
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(val).append(" billion");

            String rstr = formatter.format((int) (input % 1000000000L));
            if (!rstr.isEmpty()) {
                sb.append(" ").append(rstr);
            }
            return sb.toString();
        }
    }

}
