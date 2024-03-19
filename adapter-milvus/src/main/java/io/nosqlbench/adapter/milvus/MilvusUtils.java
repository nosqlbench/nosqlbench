/*
 * Copyright (c) 2024 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.adapter.milvus;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MilvusUtils {

    public static List<String> splitNames(String input) {
        Objects.requireNonNull(input);
        return Arrays.asList(input.split("( +| *, *)"));
    }

    public static List<Long> splitLongs(String input) {
        Objects.requireNonNull(input);
        return Arrays.stream(input.split("( +| *, *)")).map(Long::parseLong).toList();
    }


    /**
     * Mask the digits in the given string with '*'
     * @param unmasked The string to mask
     * @return The masked string
     */
    protected static String maskDigits(String unmasked) {
        int inputLength = (null == unmasked) ? 0 : unmasked.length();
        StringBuilder masked = new StringBuilder(inputLength);
        for(char ch : unmasked.toCharArray()) {
            if (Character.isDigit(ch)) {
                masked.append("*");
            } else {
                masked.append(ch);
            }
        }
        return masked.toString();
    }
}
