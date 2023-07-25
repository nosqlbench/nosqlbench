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

package io.nosqlbench.virtdata.library.basics.shared.util;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.Arrays;
import java.util.function.LongFunction;
import java.util.function.LongToDoubleFunction;


/**
 * For comprehensive docs on how this works, please see the javadocs for
 * {@link Combiner}&lt;T&gt;. This class is merely a primitive specialization.
 */
@ThreadSafeMapper
@Categories({Category.combinitoric, Category.conversion})
public class DoubleCombiner implements LongFunction<double[]> {

    /**
     * converts an index for a given column position into a value type.
     */
    private final LongToDoubleFunction elementFunction;
    /**
     * The columnar character sequences which represent radix values
     */
    private final char[][] charsets;

    /**
     * The columnar radix factors, cached
     */
    private final long[] modulo;
    /**
     * Columnar indexes from the character to the index values, for reverse mapping
     */
    private final int[][] inverted;

    /**
     * Construct a combiner which can compose unique combinations of array data.
     *
     * @param spec
     *     The string specifier, as explained in {@link DoubleCombiner} docs.
     * @param elementFunction
     *     The function that indexes into a unique population of T elements
     */
    public DoubleCombiner(String spec, LongToDoubleFunction elementFunction) {
        this.charsets = Combiner.parseSpec(spec);
        this.elementFunction = elementFunction;
        this.modulo = computeRadixFactors(charsets);
        this.inverted = Combiner.invertedIndexFor(this.charsets);
    }

    protected static long maxRadixDigits(String spec) {
        return Arrays.stream(Combiner.parseSpec(spec)).mapToInt(c->c.length).max().orElse(0);
    }

    @Override
    public double[] apply(long value) {
        double[] ary = new double[charsets.length];
        for (int colIdx = 0; colIdx < charsets.length; colIdx++) {
            int valueSelector = (int) ((value / modulo[colIdx]) % Integer.MAX_VALUE);
            ary[colIdx] = elementFunction.applyAsDouble(valueSelector);
            value %= modulo[colIdx];
        }
        return ary;
    }

    public double[] getArray(int[] indexes) {
        double[] ary = new double[charsets.length];
        for (int colIdx = 0; colIdx < indexes.length; colIdx++) {
            ary[colIdx] = elementFunction.applyAsDouble(indexes[colIdx]);
        }
        return ary;
    }

    public double[] getArray(String encoding) {
        long ordinal = getOrdinal(encoding);
        return apply(ordinal);
    }

    public String getEncoding(long ordinal) {
        return getEncoding(getIndexes(ordinal));
    }

    public String getEncoding(int[] indexes) {
        StringBuilder sb = new StringBuilder(charsets.length);
        for (int i = 0; i < indexes.length; i++) {
            sb.append(charsets[i][indexes[i]]);
        }
        return sb.toString();
    }

    /**
     * Get the indexes directly which are used by {@link #apply(long)}
     *
     * @param value
     * @return an offset array for each column in the provided charset specifiers
     */
    public int[] getIndexes(long value) {
        int[] ary = new int[charsets.length];
        for (int colIdx = 0; colIdx < charsets.length; colIdx++) {
            int valueSelector = (int) ((value / modulo[colIdx]) % Integer.MAX_VALUE);
            ary[colIdx] = valueSelector;
            value %= modulo[colIdx];
        }
        return ary;
    }

    /**
     * @param encoding
     *     the string encoding for the given ordinal
     * @return the indexes used to select a value from the value function for each position in the output array
     */
    public int[] getIndexes(String encoding) {
        int[] indexes = new int[charsets.length];
        char[] chars = encoding.toCharArray();
        for (int i = 0; i < charsets.length; i++) {
            indexes[i] = inverted[i][chars[i]];
        }
        return indexes;
    }

    /**
     * Using the provided name, derive the ordinal value which matches it.
     *
     * @param name
     *     - the textual name, expressed as an ASCII string
     * @return the long which can be used to construct the matching name or related array.
     */
    public long getOrdinal(String name) {
        char[] chars = name.toCharArray();
        long ordinal = 0;
        for (int i = 0; i < chars.length; i++) {
            ordinal += (modulo[i] * inverted[i][chars[i]]);
        }
        return ordinal;
    }

    /**
     * Using the provided column offsets, derive the ordinal value which matches it.
     *
     * @param indexes
     *     - the indexes used to derive an array of values, or equivalently a name
     * @return the long which can be used to construct the matching name or related array.
     */
    public long getOrdinal(int[] indexes) {
        long ordinal = 0;
        for (int i = 0; i < indexes.length; i++) {
            ordinal += (modulo[i] *= indexes[i]);
        }
        return ordinal;
    }


    public static long[] computeRadixFactors(char[][] charsets) {
        long modulo = 1L;
        long[] m = new long[charsets.length];
        for (int i = charsets.length - 1; i >= 0; i--) {
            m[i] = modulo;
            modulo = Math.multiplyExact(modulo, charsets[i].length);
        }
//        m[m.length-1]=modulo;
        return m;
    }

}
