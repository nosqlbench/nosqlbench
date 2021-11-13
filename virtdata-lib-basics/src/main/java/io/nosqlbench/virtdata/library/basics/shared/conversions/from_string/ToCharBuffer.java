package io.nosqlbench.virtdata.library.basics.shared.conversions.from_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.nio.CharBuffer;
import java.util.function.Function;

/**
 * Convert the input string to a character buffer
 */
@ThreadSafeMapper
@Categories(Category.conversion)
public class ToCharBuffer implements Function<String, CharBuffer> {

    @Override
    public CharBuffer apply(String s) {
        return CharBuffer.wrap(s);
    }
}
