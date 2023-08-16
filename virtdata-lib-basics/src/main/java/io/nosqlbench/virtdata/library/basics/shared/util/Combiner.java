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

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.LongFunction;


/**
 * <H1>Combiner - a <A href="https://en.wikipedia.org/wiki/Combinatorics">combinatoric</A> toolkit for NoSQLBench</H1>
 * <HR></HR>
 * <H2>Synopsis</H2>
 * <P>Combiner is the core implementation of a combinatoric toolkit which is used
 * by other NoSQLBench functions in a more type-specific way. It allows for a common approach
 * to encoding unique values across a range of dimensions (which can be non-uniform)
 * with an affine mapping between different forms of data.</P>
 *
 * <HR></HR>
 * <H2>Specifier</H2>
 * <P>The specifier required by the constructor is a way to specify a range of character sets, each representing both
 * the per-value labeling as well as the radix of each position in the associated index, value, or character position.
 * Each position is delimited from the others with commas or semicolons. Each position can be either a single printable
 * character or a range of characters separated by
 * '-'.
 * Optionally, you can repeat a position with a multiplier in the form of '*n' where n is any valid number.
 * </P>
 * <P>Examples:
 * <UL>
 * <LI>"0-9A-F" - hexadecimal characters, one digit only ; 0123456789ABCDEF</LI>
 * <LI>"0-9*12" - characters 0-9 in 12 digits, symbolic of values 000000000000 (0) .. 999999999999</LI>
 * <LI>"5;5;5;-;8;6;7;-;5;3;0;9" - 12 digits with one character each, effectively 555-867-5309, a single value</LI>
 * <LI>"0-9;*2_=24j36*5*1" - a somewhat random pattern with char sets [0123456789] and [*2_=24j36*5], showing how '*1'
 * at the end can be used to escape '*5'</LI>
 * </UL>
 * The specifier is parsed into a non-uniform radix model, where the characters for each position represent a numerical
 * encoding. As such, the cardinalities of each position are multiplied together to determine the total cardinality of
 * the specified pattern. Any total cardinality below Long.MAX_VALUE, or 9,223,372,036,854,775,807 is allowed, and any
 * combinations which would overflow this value will throw an error.
 * </P>
 *
 * <HR></HR>
 * <H2>Value Function</H2>
 * <p>
 * The function provided in the constructor is used to symbolically map the characters in the encoding string to a value
 * of any type. The value function will be called with number of distinct values up the the cardinality of the largest
 * position in the radix model. For example, a specifier of `A-Za-z0-9` would provide an input range from 0 to 61
 * inclusive to the value function. It is the combination of positions and unique values which provides the overall
 * cardinality, although the value function itself is responsible for the relatively lower cardinality elements which
 * are combined together to create higher-cardinality value arrays.
 * </P>
 *
 * <HR></HR>
 * <H2>Types and Forms</H2>
 *
 * <P>Each form represents one way of seeing the data for a given cycle:
 * <OL>
 * <LI><B>ordinal</B> (long) - also known as the cycle, or input. This is an enumeration of all distinct
 * combinations.</LI>
 * <LI><B>indexes</B> (int[]) - an array of indexes, one for each position in the specifier and thus each element in
 * the
 * array
 * or character in the encoding.</LI>
 * <LI><B>encoding</B> (String) - a string which encodes the ordinal and the indexes in a convenient label which is
 * unique
 * within the range of possible values.</LI>
 * <LI><B>(values) array (T[])</B> - An array of the type T which can be provided via a mapping function. This is a
 * mapping from the
 * indexes through the provided value function.</LI>
 * </OL>
 * </P>
 *
 * <HR></HR>
 * <H2>Mapping between forms</H2>
 *
 * <P>The array value can be derived with {@link #apply(long)}, {@link #getArray(int[])} (int[])}, and
 * {@link #getArray(String)},
 * given ordinal, indexes, or encoding as a starting point, respectively. This all ultimately use the one-way
 * function which you provide, thus you can't go from array form to the others.</P>
 *
 * <P>Mapping between the other three is fairly trivial:</P>
 * <UL>
 * <LI>You can get indexes from ordinal and encoding with {@link #getIndexes(long)} and
 * {@link #getArray(String)}.</LI>
 * <LI>You can get encoding from ordinal and indexes with {@link #getEncoding(long)} and
 * {@link #getEncoding(int[])}.</LI>
 * <LI>You can get ordinal from indexes or encoding with {@link #getOrdinal(int[])} and
 * {@link #getOrdinal(String)}.</LI>
 * </UL>
 * </P>
 * <p>
 * This makes it easy to derive textual identifiers for specific combinations of elements such as a vector, use them
 * for
 * cross-checks such as with correctness testing, and represent specific test values in a very convenient form within
 * deterministic testing harnesses like NoSQLBench.
 *
 * @param <T>
 *     The generic type of the value which is mapped into each array position
 */
@ThreadSafeMapper
@Categories({Category.combinitoric, Category.conversion})
public class Combiner<T> implements LongFunction<T[]> {

    /**
     * converts an index for a given column position into a value type.
     */
    private final LongFunction<T> elementFunction;
    /**
     * Used for instancing the correct type of array, since arrays can't be reified from generics
     */
    private final Class<? extends T> elementClazz;
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
     *     The string specifier, as explained in {@link Combiner} docs.
     * @param elementFunction
     *     The function that indexes into a unique population of T elements
     * @param elementClazz
     *     The component type for the values array which are produced by {@link #apply(long)}
     */
    public Combiner(String spec, LongFunction<T> elementFunction, Class<? extends T> elementClazz) {
        this.charsets = Combiner.parseSpec(spec);
        this.elementFunction = elementFunction;
        this.elementClazz = elementClazz;
        this.modulo = computeRadixFactors(charsets);
        this.inverted = invertedIndexFor(this.charsets);
    }


    /**
     * Parse the spec, yielding an array of character arrays. each position in the spec delimited
     * by comma or semicolon is represented by an array. Each array is then constructed from
     * {@link #rangeFor(String)}.
     *
     * @param rangesSpec
     *     A range set specifier
     * @return An array of char arrays
     */
    public static char[][] parseSpec(String rangesSpec) {
        String[] ranges = rangesSpec.split("[,;]");
        List<String> specs = new ArrayList<>();
        for (String range : ranges) {
            if (range.matches("(.*?)\\*(\\d+)")) {
                int rangeAt = range.lastIndexOf('*');
                int times = Integer.parseInt(range.substring(rangeAt + 1));
                for (int i = 0; i < times; i++) {
                    specs.add(range.substring(0, rangeAt));
                }
            } else {
                specs.add(range);
            }
        }
        char[][] cs = new char[specs.size()][];
        for (int i = 0; i < specs.size(); i++) {
            char[] range = rangeFor(specs.get(i));
            cs[i] = range;
        }
        return cs;
    }

    /**
     * Parse the range and return set of characters in an array. Any occurrences of a range specifier
     * like {@code a-z} are expanded into the two characters and every on in between, in ordinal order.
     * Otherwise, the characters are taken as they are presented. Each range is built and sanity
     * checked by {@link #rangeFor} to ensure ordering is valid as well as that the characters are
     * all in the printable range of ordinal 32 to ordinal 126.
     *
     * @param range
     *     a character range specifier like 'a-z' or '1357'
     * @return An array of characters
     */
    public static char[] rangeFor(String range) {
        range = range.replaceAll("\\n", "\n").replaceAll("\\r", "\r");
        List<Character> chars = new ArrayList<>();
        int pos = 0;
        while (pos < range.length()) {
            if (range.length() > pos + 2 && range.charAt(pos + 1) == '-') {
                List<Character> rangeChars = rangeFor(range.substring(pos, pos + 1), range.substring(pos + 2, pos + 3));
                chars.addAll(rangeChars);
                pos += 3;
            } else {
                chars.add(range.substring(pos, pos + 1).charAt(0));
                pos += 1;
            }
        }
        char[] charAry = new char[chars.size()];
        for (int i = 0; i < chars.size(); i++) {
            charAry[i] = chars.get(i);
        }
        return charAry;
    }

    /**
     * Create a list of characters from the US ASCII plane based on a start and end character.
     *
     * @param startChar
     *     A single ASCII character
     * @param endChar
     *     A single ASCII character, must be equal to or come after startChar
     * @return A list of characters in the range
     */
    public static List<Character> rangeFor(String startChar, String endChar) {
        int start = startChar.getBytes(StandardCharsets.US_ASCII)[0];
        int end = endChar.getBytes(StandardCharsets.US_ASCII)[0];
        assertPrintable(start);
        assertPrintable(end);
        assertOrder(start, end);
        List<Character> chars = new ArrayList<>();
        ByteBuffer bb = ByteBuffer.allocate(1);
        for (int i = start; i <= end; i++) {
            bb.clear();
            bb.put(0, (byte) i);
            CharBuffer decoded = StandardCharsets.US_ASCII.decode(bb);
            chars.add(decoded.get(0));
        }
        return chars;
    }

    private static void assertOrder(int start, int end) {
        if (end < start) {
            throw new RuntimeException("char '" + (char) end + "' (" + end + ") occurs after '" + (char) start + "' (" + start + "). Are you sure this is the right spec? (reverse the order)");
        }

    }

    private static void assertPrintable(int asciiCode) {
        if (asciiCode > 126 || asciiCode < 32) {
            throw new RuntimeException("ASCII character for code " + asciiCode + " is outside the range of printable characters.");
        }
    }

    public static int[][] invertedIndexFor(String charsetsSpecifier) {
        char[][] chars = parseSpec(charsetsSpecifier);
        return invertedIndexFor(chars);
    }

    public static int[][] invertedIndexFor(char[][] charsetColumns) {
        int[][] inverted = new int[charsetColumns.length][];
        for (int charsetIdx = 0; charsetIdx < charsetColumns.length; charsetIdx++) {
            char[] charsForColumn = charsetColumns[charsetIdx];
            inverted[charsetIdx] = indexesByChar(charsForColumn);
        }
        return inverted;
    }

    private static int[] indexesByChar(char[] charsForColumn) {
        int maxval = Integer.MIN_VALUE;
        for (char c : charsForColumn) {
            maxval = (int) c > maxval ? (int) c : maxval;
        }
        int[] idx = new int[maxval + 1];
        Arrays.fill(idx, -1);

        for (int i = 0; i < charsForColumn.length; i++) {
            idx[charsForColumn[i]] = i;
        }
        return idx;
    }

    /**
     * Return an array of {@link T} elements by indexing into the sequence
     * of character sets and their relative cardinality to derive column-specific
     * index, and then converting them to the type T through the provided function.
     *
     * @param value
     *     the function argument
     * @return a T which is identified by the provided value, unique if value is
     *     less than the maximum number of combinations, but repeated otherwise
     */
    @Override
    public T[] apply(long value) {
        @SuppressWarnings("Unchecked")
        T[] ary = (T[]) Array.newInstance(elementClazz, charsets.length);
        for (int colIdx = 0; colIdx < charsets.length; colIdx++) {
            int valueSelector = (int) ((value / modulo[colIdx]) % Integer.MAX_VALUE);
            ary[colIdx] = elementFunction.apply(valueSelector);
            value %= modulo[colIdx];
        }
        return ary;
    }

    /**
     * @param indexes
     *     indexes derived from {@link #getIndexes(long)}
     * @return a T[]
     */
    public T[] getArray(int[] indexes) {
        T[] ary = (T[]) Array.newInstance(elementClazz, charsets.length);
        for (int colIdx = 0; colIdx < indexes.length; colIdx++) {
            ary[colIdx] = elementFunction.apply(indexes[colIdx]);
        }
        return ary;
    }

    public T[] getArray(String encoding) {
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
