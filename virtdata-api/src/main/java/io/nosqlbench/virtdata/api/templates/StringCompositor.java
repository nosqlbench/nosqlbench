package io.nosqlbench.virtdata.api.templates;

import io.nosqlbench.virtdata.api.ValuesBinder;
import io.nosqlbench.virtdata.api.Bindings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * StringCompositor provides a way to build strings from a string template and provided values.
 *
 * <p>
 * The template is simply an array of string values, where odd indices represent token positions, and even indices represent
 * literals. This version of the StringCompositor fetches data from the bindings only for the named fields in the template.
 * </p>
 */
public class StringCompositor implements ValuesBinder<StringCompositor, String> {

//    protected static Pattern tokenPattern = Pattern.compile("(?<section>(?<literal>([^{])+)?(?<anchor>\\{(?<token>[a-zA-Z0-9-_.]+)?\\})?)");
    private String[] templateSegments;
    private Function<Object, String> stringfunc = String::valueOf;

    /**
     * Create a string template which has positional tokens, in "{}" form.
     *
     * @param template The string template
     */
    public StringCompositor(String template) {
        templateSegments = parseTemplate(template);
    }

    public StringCompositor(String template, Function<Object, String> stringfunc) {
        this(template);
        this.stringfunc = stringfunc;
    }

    // for testing
    protected String[] parseTemplate(String template) {
        ParsedTemplate parsed = new ParsedTemplate(template, Collections.emptyMap());
        return parsed.getSpans();
    }

//    // for testing
//    protected String[] parseSection(String template) {
//        StringBuilder literalBuf = new StringBuilder();
//        int i = 0;
//        for (; i < template.length(); i++) {
//            char c = template.charAt(i);
//            if (c == '\\') {
//                i++;
//                c = template.charAt(i);
//                literalBuf.append(c);
//            } else if (c != '{') {
//                literalBuf.append(c);
//            } else  {
//                i++;
//                break;
//            }
//        }
//        StringBuilder tokenBuf = new StringBuilder();
//        for (; i < template.length(); i++) {
//            char c = template.charAt(i);
//            if (c != '}') {
//                tokenBuf.append(c);
//            } else {
//                i++;
//                break;
//            }
//        }
//        String literal=literalBuf.toString();
//        String token = tokenBuf.toString();
//        if (token.length()>0) {
//            return new String[] { literalBuf.toString(), tokenBuf.toString(), template.substring(i)};
//        } else {
//            return new String[] { literalBuf.toString() };
//        }
//    }
//
//    /**
//     * Parse the template according to the description for {@link StringCompositor}.
//     *
//     * @param template A string template.
//     * @return A template array.
//     */
//    protected String[] parseTemplate(String template) {
//        List<String> sections = new ArrayList<>();
//
//        String[] parts = parseSection(template);
//        while (parts.length>0) {
//            sections.add(parts[0]);
//            if (parts.length>1) {
//                sections.add(parts[1]);
//            }
//            parts = parts.length>=2 ? parseSection(parts[2]) : new String[0];
//        }
//        if ((sections.size() % 2) == 0) {
//            sections.add("");
//        }
//        return sections.toArray(new String[0]);
//    }

    @Override
    public String bindValues(StringCompositor context, Bindings bindings, long cycle) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < templateSegments.length; i++) {
            if (i % 2 == 0) {
                sb.append(templateSegments[i]);
            } else {
                String key = templateSegments[i];
                Object value = bindings.get(key, cycle);
                String valueString = stringfunc.apply(value);
                sb.append(valueString);
            }
        }
        return sb.toString();
    }

    public List<String> getBindPointNames() {
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < templateSegments.length; i++) {
            if (i % 2 == 1) {
                tokens.add(templateSegments[i]);
            }
        }
        return tokens;
    }
}
