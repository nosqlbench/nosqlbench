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

package io.virtdata.libbasics.shared.from_long.to_collection;


import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.shared.from_long.to_int.HashRange;
import io.virtdata.libbasics.shared.from_long.to_string.HashedLineToString;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;

/**
 * Create a String-String map from the specified file, ranging in size
 * from 0 to the specified maximum.
 */
@ThreadSafeMapper
@Categories({Category.collections})
public class HashedLineToStringStringMap implements LongFunction<Map<String,String>> {

    private final HashedLineToString lineDataMapper;
    private final HashRange sizeRange;

    public HashedLineToStringStringMap(String paramFile, int maxSize) {

        this.sizeRange = new HashRange(0, maxSize-1);
        this.lineDataMapper = new HashedLineToString(paramFile);
    }

    @Override
    public Map<String, String> apply(long input) {
        int mapSize = sizeRange.applyAsInt(input);
        Map<String,String> map = new HashMap<>();
        for (int idx=0;idx<mapSize;idx++) {
            map.put(lineDataMapper.apply(input), lineDataMapper.apply(input));
        }
        return map;
    }

}
