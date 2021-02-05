package io.nosqlbench.virtdata.library.basics.shared.unary_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import org.apache.commons.text.StringEscapeUtils;

import java.util.function.Function;

/**
 * encode HTML Entities
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class HTMLEntityEncode implements Function<String, String> {

    @Example({"HTMLEntityEncode()", "Encode/Escape input into HTML4 valid entties."})
    public HTMLEntityEncode() {}

    @Override
    public String apply(String s) {
        return StringEscapeUtils.escapeHtml4(s);
    }
}
