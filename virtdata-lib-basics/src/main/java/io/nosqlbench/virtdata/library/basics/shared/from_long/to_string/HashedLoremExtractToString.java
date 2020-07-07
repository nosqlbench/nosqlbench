/*
 *
 *       Copyright 2015 Jonathan Shook
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.pache.org/licenses/LICENSE-2.0
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

import java.util.function.LongFunction;

/**
 * Provide a text extract from the full lorem ipsum text, between the specified
 * minimum and maximum size.
 *
 */
@ThreadSafeMapper
public class HashedLoremExtractToString implements LongFunction<String> {

    private final HashedFileExtractToString randomFileExtractMapper;

    public HashedLoremExtractToString(int minsize, int maxsize) {
        randomFileExtractMapper = new HashedFileExtractToString("data/lorem_ipsum_full.txt", minsize, maxsize);
    }

    @Override
    public String apply(long input) {
        return randomFileExtractMapper.apply(input);
    }

}
