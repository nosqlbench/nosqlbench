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
public class HTMLEntityDecode implements Function<String, String> {

    @Example({"HTMLEntityEncode()", "Decode/Unescape input from HTML4 valid to text."})
    public HTMLEntityDecode() {}

    @Override
    public String apply(String s) {
        return StringEscapeUtils.unescapeHtml4(s);
    }
}
