package io.nosqlbench.virtdata.library.basics.shared.unary_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replace all occurrences of the regular expression with the replacement string.
 * Note, this is much less efficient than using the simple ReplaceAll for most cases.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class ReplaceRegex implements Function<String, String> {

    private final String replacement;
    private final Pattern pattern;

    @Example({"ReplaceRegex('[one]','two')", "Replace all occurrences of 'o' or 'n' or 'e' with 'two'"})
    public ReplaceRegex(String regex, String replacement) {
        this.pattern = Pattern.compile(regex);
        this.replacement = replacement;
    }

    @Override
    public String apply(String s) {
        Matcher matcher = pattern.matcher(s);
        StringBuilder sb = new StringBuilder(s.length());
        while (matcher.find()) {
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
