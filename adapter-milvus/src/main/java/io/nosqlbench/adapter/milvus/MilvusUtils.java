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

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class MilvusUtils {

    public static List<String> splitNames(String input) {
        assert StringUtils.isNotBlank(input) && StringUtils.isNotEmpty(input);
        return Arrays.stream(input.split("( +| *, *)"))
            .filter(StringUtils::isNotBlank)
            .toList();
    }

    public static List<Long> splitLongs(String input) {
        assert StringUtils.isNotBlank(input) && StringUtils.isNotEmpty(input);
        return Arrays.stream(input.split("( +| *, *)"))
            .filter(StringUtils::isNotBlank)
            .map(Long::parseLong)
            .toList();
    }


    /**
     * Mask the digits in the given string with '*'
     *
     * @param unmasked The string to mask
     * @return The masked string
     */
    protected static String maskDigits(String unmasked) {
        assert StringUtils.isNotBlank(unmasked) && StringUtils.isNotEmpty(unmasked);
        int inputLength = unmasked.length();
        StringBuilder masked = new StringBuilder(inputLength);
        for (char ch : unmasked.toCharArray()) {
            if (Character.isDigit(ch)) {
                masked.append("*");
            } else {
                masked.append(ch);
            }
        }
        return masked.toString();
    }
}
