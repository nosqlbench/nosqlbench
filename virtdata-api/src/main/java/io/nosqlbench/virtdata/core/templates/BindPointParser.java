package io.nosqlbench.virtdata.core.templates;

import io.nosqlbench.nb.api.errors.BasicError;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BindPointParser implements Function<String, List<String>> {

    public final static Pattern BINDPOINT_ANCHOR = Pattern.compile("(\\{((?<anchor>\\w+[-_\\d\\w.]*)})|(\\{\\{(?<extended>[^}]+)}}))");

    @Override
    public List<String> apply(String template) {

        Matcher m = BINDPOINT_ANCHOR.matcher(template);
        int lastMatch = 0;
        List<String> spans = new ArrayList<>();

        while (m.find()) {
            String pre = template.substring(lastMatch, m.start());
            lastMatch=m.end();
            spans.add(pre);

            String anchor = m.group("anchor");
            if (anchor == null) {
                anchor = m.group("extended");
                if (anchor == null) {
                    throw new BasicError("Unable to parse: " + template);
                }
            }
            spans.add(anchor);
        }
        spans.add(lastMatch >= 0 ? template.substring(lastMatch) : template);

        return spans;
    }

}
