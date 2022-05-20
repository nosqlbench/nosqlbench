package io.nosqlbench.virtdata.library.basics.shared.util;

/*
 * Copyright (c) 2022 nosqlbench
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


import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CharsetMapping {

    /**
     * Parse the spec, yielding an array of character arrays. each position in the spec delimited
     * by comma or semicolon is represented by an array. Each array is then constructed from
     * {@link #rangeFor(String)}.
     *
     * @param spec A range set specifier
     * @return An array of char arrays
     */
    public static char[][] parseSpec(String spec) {
        String[] ranges = spec.split("[,;]");
        char[][] cs = new char[ranges.length][];
        for (int i = 0; i < ranges.length; i++) {
            char[] range = rangeFor(ranges[i]);
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
     * @param range a character range specifier like 'a-z' or '1357'
     * @return An array of characters
     */
    public static char[] rangeFor(String range) {
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
     * @param startChar A single ASCII character
     * @param endChar A single ASCII character, must be equal to or come after startChar
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



}
