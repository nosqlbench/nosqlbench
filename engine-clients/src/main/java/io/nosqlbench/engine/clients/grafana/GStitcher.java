package io.nosqlbench.engine.clients.grafana;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GStitcher {

    private final Map<String, Set<String>> values;
    private final static Pattern pattern = Pattern.compile("\\$(\\w+)");

    public GStitcher(Map<String, Set<String>> values) {
        this.values = values;
    }

    public String stitchRegex(String spec) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = pattern.matcher(spec);
        while (matcher.find()) {
            String word = matcher.group(1);
            if (values.containsKey(word)) {
                Set<String> elems = values.get(word);
                String replacement = "(" + String.join("|", elems) + ")";
                matcher.appendReplacement(sb, replacement);
            } else {
                matcher.appendReplacement(sb, "NOTFOUND[" + word + "]");
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
