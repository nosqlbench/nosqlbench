package io.virtdata.libbasics.shared.formatting;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

/**
 * Apply the Java String.format method to an incoming object.
 * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html#syntax">Java 8 String.format(...) javadoc</a>
 * Note: This function can often be quite slow, so more direct methods are generally preferrable.
 */
@ThreadSafeMapper
@Categories(Category.conversion)
public class Format implements Function<Object,String> {

    private final String format;

    @Example({"Format('Y')","Yield the formatted year from a Java date object."})
    public Format(String format) {
        this.format = format;
    }

    @Override
    public String apply(Object o) {
        return String.format(format,o);
    }
}
