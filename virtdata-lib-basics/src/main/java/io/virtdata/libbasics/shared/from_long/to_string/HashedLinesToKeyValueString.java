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

package io.virtdata.libbasics.shared.from_long.to_string;

import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.shared.from_long.to_collection.HashedLineToStringStringMap;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Map;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

/**
 * Generate a string in the format key1:value1;key2:value2;... from the words
 * in the specified file, ranging in size between zero and the specified maximum.
 */
@ThreadSafeMapper
public class HashedLinesToKeyValueString implements LongFunction<String> {
    private static final Logger logger = LogManager.getLogger(HashedLinesToKeyValueString.class);

    private final HashedLineToStringStringMap lineDataMapper;

    public HashedLinesToKeyValueString(String paramFile, int maxsize) {
        lineDataMapper = new HashedLineToStringStringMap(paramFile, maxsize);
    }

    @Override
    public String apply(long input) {
        Map<String, String> stringStringMap = lineDataMapper.apply(input);
        String mapstring = stringStringMap.entrySet().stream().
                map(es -> es.getKey() + ":" + es.getValue() + ";")
                .collect(Collectors.joining());
        return mapstring;
    }

}
