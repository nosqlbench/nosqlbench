package io.nosqlbench.virtdata.library.basics.shared.from_long.to_int;

/*
 * Copyright (c) nosqlbench
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;

/**
 * SequenceOf bindings allow you to specify an order and count of a set of values which will then
 * be repeated in that order.
 */
@ThreadSafeMapper
@Categories(Category.experimental)
public class SequenceOf implements LongToIntFunction {
    private final static Logger logger = LogManager.getLogger(SequenceOf.class);

    final int[] sequence;
    /**
     * <p>
     * This function produces values from a lookup table for direct control of numerical sequences.
     * The sequence spec is a string containing the sequence values and their occurences, defaulting to 1 each.
     * Example: "1:6 2 3 4 5", which means "1 at a relative frequency of 6 and 2, 3, 4, and 5 at a relative frequency
     * of 1 each. This will yield pattern "1, 1, 1, 1, 1, 1, 2, 3, 4, 5, 1, 1, 1, 1, 1, 1, 2, 3, 4, 5, ..."
     * </p>
     *
     * <p>
     * Each implementation of {@link SequenceOf} must include a type sigil as the first parameter to disambiguate
     * it from the others.
     * </p>
     *
     * @param ignored
     *     any long value, discarded after signature matching. The exampleValue is thrown away, but is necessary for
     *     matching the right version of SequenceOf.
     * @param spec
     *     A string of numbers separated by spaces, semicolons, or commas. This is the sequence spec..
     */
    @Example({"SequenceOf(1L,'3:3 2:2 1:1')","Generate sequence 3,3,3,2,2,1"})
    @Example({"SequenceOf(1L,'1000:99 1000000:1')","Generate sequence 1000 (99 times) and then 1000000 (1 time)"})
    public SequenceOf(int ignored, String spec) {
        this.sequence = parseSequence(spec);

    }

    public static int[] parseSequence(String input) {
        String[] entries = input.split("[;, ]");
        int[][] subarys = new int[entries.length][];
        int entry=0;
        int size=0;

        String[] parts;
        for (int i = 0; i < entries.length; i++) {
            parts = entries[i].split(":");
            int value = Integer.parseInt(parts[0]);
            int count = (parts.length==1) ? 1 : Integer.parseInt(parts[1]);
            int[] segment = new int[count];
            Arrays.fill(segment,value);
            subarys[entry++]=segment;
            size+=segment.length;
        }
        if (size>1E6) {
            logger.warn("The sequence you have specified is very large, which may cause problems. You should consider" +
                " a different approach for this type of function.");
        }
        int[] sequence = new int[size];
        int offset=0;
        for (int[] subary : subarys) {
            System.arraycopy(subary,0,sequence,offset,subary.length);
            offset+=subary.length;
        }
        return sequence;
    }

    @Override
    public int applyAsInt(long value) {
        return sequence[(int) value % sequence.length];
    }
}
