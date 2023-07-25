/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.util.Combiner;

import java.nio.CharBuffer;
import java.util.function.LongFunction;

/**
 * Convert a numeric value into a code according to ASCII printable
 * characters. This is useful for creating various encodings using different
 * character ranges, etc.
 *
 * This mapper can map over the sequences of character ranges providing every unique
 * combination and then wrapping around to the beginning again.
 * It can convert between character bases with independent radix in each position.
 * Each position in the final string takes its values from a position-specific
 * character set, described by the shorthand in the examples below.
 *
 * The constructor will throw an error if the number of combinations exceeds that
 * which can be represented in a long value. (This is a very high number).
 */
@ThreadSafeMapper
@Categories({Category.general})
public class Combinations implements LongFunction<String> {

    private final char[][] charsets;
    private final long[] modulo;

    @Example({"Combinations('A-Z;A-Z')","a two digit alphanumeric code. Wraps at 26^2"})
    @Example({"Combinations('0-9A-F')","a single hexadecimal digit"})
    @Example({"Combinations('0123456789ABCDEF')","a single hexadecimal digit"})
    @Example({"Combinations('0-9A-F;0-9A-F;0-9A-F;0-9A-F;')","two bytes of hexadecimal"})
    @Example({"Combinations('A-9')","upper case alphanumeric"})
    public Combinations(String spec) {
        this.charsets = Combiner.parseSpec(spec);
        this.modulo = Combiner.computeRadixFactors(this.charsets);
    }

    @Override
    public String apply(long value) {
        CharBuffer cb = CharBuffer.allocate(charsets.length);
        for (int cs = 0; cs < charsets.length; cs++) {
            int charv = (int) ((value / modulo[cs]) % Integer.MAX_VALUE);
            value %= modulo[cs];
            int selector = charv % charsets[cs].length;
            char c = charsets[cs][selector];
            cb.put(cs, c);
        }
        return cb.toString();
    }

}
