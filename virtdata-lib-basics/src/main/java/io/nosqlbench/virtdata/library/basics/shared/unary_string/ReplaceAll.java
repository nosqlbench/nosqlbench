package io.nosqlbench.virtdata.library.basics.shared.unary_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.Function;

/**
 * Replace all occurrences of the extant string with the replacement string.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class ReplaceAll implements Function<String, String> {

    private final String extant;
    private final String replacement;

    @Example({"ReplaceAll('one','two')", "Replace all occurrences of 'one' with 'two'"})
    public ReplaceAll(String extant, String replacement) {
        this.extant = extant;
        this.replacement = replacement;
    }

    @Override
    public String apply(String s) {
        return s.replaceAll(extant, replacement);
    }
}
