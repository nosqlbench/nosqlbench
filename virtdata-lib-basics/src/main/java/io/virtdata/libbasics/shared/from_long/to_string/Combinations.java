package io.virtdata.libbasics.shared.from_long.to_string;

import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
public class Combinations implements LongFunction<String> {

    private final char[][] charsets;
    private final long[] modulo;

    @Example({"Combinations('A-Z;A-Z')","a two digit alphanumeric code. Wraps at 26^2"})
    @Example({"Combinations('0-9A-F')","a single hexadecimal digit"})
    @Example({"Combinations('0123456789ABCDEF')","a single hexadecimal digit"})
    @Example({"Combinations('0-9A-F;0-9A-F;0-9A-F;0-9A-F;')","two bytes of hexadecimal"})
    @Example({"Combinations('A-9')","upper case alphanumeric"})
    public Combinations(String spec) {
        this.charsets = parseSpec(spec);
        this.modulo = computeRadixFactors(this.charsets);
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

    private long[] computeRadixFactors(char[][] charsets) {
        long modulo = 1L;
        long[] m = new long[charsets.length];
        for (int i = charsets.length-1; i >=0; i--) {
            m[i] = modulo;
            modulo = Math.multiplyExact(modulo, charsets[i].length);
        }
//        m[m.length-1]=modulo;
        return m;
    }

    private char[][] parseSpec(String spec) {
        String[] ranges = spec.split("[,;]");
        char[][] cs = new char[ranges.length][];
        for (int i = 0; i < ranges.length; i++) {
            char[] range = rangeFor(ranges[i]);
            cs[i] = range;
        }
        return cs;
    }

    private char[] rangeFor(String range) {
        List<Character> chars = new ArrayList<>();
        int pos = 0;
        while (pos < range.length()) {
            if (range.length() > pos + 2 && range.substring(pos + 1, pos + 2).equals("-")) {
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

    private List<Character> rangeFor(String startChar, String endChar) {
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

    private void assertOrder(int start, int end) {
        if (end < start) {
            throw new RuntimeException("char '" + (char) end + "' (" + end + ") occurs after '" + (char) start + "' (" + start + "). Are you sure this is the right spec? (reverse the order)");
        }

    }

    private void assertPrintable(int asciiCode) {
        if (asciiCode > 126 || asciiCode < 32) {
            throw new RuntimeException("ASCII character for code " + asciiCode + " is outside the range of printable characters.");
        }
    }

}
