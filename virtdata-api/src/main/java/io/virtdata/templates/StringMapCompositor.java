package io.virtdata.templates;

import io.virtdata.api.ValuesArrayBinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StringCompositor provides a way to build strings from a string template and provided values.
 *
 * <p>
 * The template is simply an array of string values, where odd indices represent literals, and even indices represent token
 * positions. If the token positions contains words, then these can be used as keys for looking up values in an associated
 * map. If not, then values are simply positional, in which case simple numerals are used as indices for debugging purposes,
 * although they are not referenced during string interpolation.
 * </p>
 */
public class StringMapCompositor implements ValuesArrayBinder<StringMapCompositor, String> {

//    private static Pattern tokenPattern = Pattern.compile("(?<!\\\\)\\{([^}]*)\\}(.*?)?",Pattern.DOTALL);
    private static Pattern tokenPattern = Pattern.compile("(?<section>(?<literal>[^{}]+)?(?<anchor>\\{(?<token>[a-zA-Z.-]+)?\\})?)");
    private String[] templateSegments;
    private int buffersize=0;

    /**
     * Create a string template which has positional tokens, in "{}" form.
     * @param template The string template
     */
    public StringMapCompositor(String template) {
        templateSegments =parseTemplate(template);
    }

    /**
     * Parse the template according to the description for {@link StringMapCompositor}.
     *
     * @param template A string template.
     * @return A template array.
     */
    private String[] parseTemplate(String template) {
        Matcher matcher = tokenPattern.matcher(template);
        List<String> sections = new ArrayList<>();
        int previous=0;
        int counter=0;
        while (matcher.find()) {
            String literal = matcher.group("literal");
            String anchor = matcher.group("anchor");
            String token = matcher.group("token");
            if (anchor==null && literal==null) {
                break;
            }
            sections.add(Optional.ofNullable(literal).orElse(""));
            if (anchor!=null) {
                sections.add(Optional.ofNullable(token).orElse(String.valueOf(counter++)));
            }
        }
        if ((sections.size()%2)==0) {
            sections.add("");
        }
        return sections.toArray(new String[0]);
    }

    @Override
    public String bindValues(StringMapCompositor context, Object[] values) {
        StringBuilder sb = new StringBuilder(buffersize);
        int len=values.length;
        if (values.length != templateSegments.length>>1) {
            throw new RuntimeException("values array has " + values.length + " elements, but "
            + " the template needs " + (templateSegments.length>>1));
        }
        sb.append(templateSegments[0]);

        for (int i = 0; i < len; i++) {
            sb.append(values[i]);
            sb.append(templateSegments[((2*i)+2)]);
        }

        if (sb.length()>buffersize) {
            buffersize=sb.length()+5;
        }

        return sb.toString();
    }
}
