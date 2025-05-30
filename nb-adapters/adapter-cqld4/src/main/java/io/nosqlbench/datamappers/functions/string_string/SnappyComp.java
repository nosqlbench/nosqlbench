/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.datamappers.functions.string_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Function;

/**
 * Compress the input using snappy compression
 */
@Categories({Category.conversion})
@ThreadSafeMapper
public class SnappyComp implements Function<String, ByteBuffer> {

    private final Snappy snappy = new Snappy();

    @Override
    public ByteBuffer apply(String s) {
        try {
            return ByteBuffer.wrap(Snappy.compress(s));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
