package io.virtdata.libbasics.shared.from_long.to_string;

import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.shared.from_long.to_long.Hash;

import java.util.function.LongFunction;

/**
 * Create an alpha-numeric string of the specified length, character-by-character.
 */
@ThreadSafeMapper
public class AlphaNumericString implements LongFunction<String> {
    private static final String AVAILABLE_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final ThreadLocal<StringBuilder> threadStringBuilder = ThreadLocal.withInitial(StringBuilder::new);
    private final Hash hash = new Hash();
    private final int length;

    public AlphaNumericString(int length)
    {
        if (length < 0)
        {
            throw new RuntimeException("AlphaNumericString must have length >= 0");
        }
        this.length = length;
    }

    @Override
    public String apply(long operand)
    {
        long hashValue = operand;
        StringBuilder sb = threadStringBuilder.get();
        sb.setLength(0);
        for (int i = 0; i < length; i++)
        {
            hashValue = hash.applyAsLong(hashValue);
            int randomPos = (int) (hashValue % AVAILABLE_CHARS.length());
            sb.append(AVAILABLE_CHARS.charAt(randomPos));
        }
        return sb.toString();
    }

    @Override
    public String toString()
    {
        return "AlphaNumericString(length=" + length + ")";
    }
}
