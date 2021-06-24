package io.nosqlbench.virtdata.core.templates;

import io.nosqlbench.nb.api.errors.BasicError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BindPointParser implements BiFunction<String, Map<String,String>, BindPointParser.Result> {

    public final static Pattern BINDPOINT_ANCHOR = Pattern.compile("(\\{((?<anchor>\\w+[-_\\d\\w.]*)})|(\\{\\{(?<extended>[^}]+)}}))");
    public final static String DEFINITION = "DEFINITION";


    @Override
    public Result apply(String template, Map<String,String> bindings) {

        Matcher m = BINDPOINT_ANCHOR.matcher(template);
        int lastMatch = 0;
        List<String> spans = new ArrayList<>();
        List<BindPoint> bindpoints = new ArrayList<>();

        while (m.find()) {
            String pre = template.substring(lastMatch, m.start());
            spans.add(pre);
            lastMatch=m.end();

            String anchor = m.group("anchor");
            String extendedAnchor = m.group("extended");
            if (anchor!=null) {
                bindpoints.add(BindPoint.of(anchor, bindings.getOrDefault(anchor, null), BindPoint.Type.reference));
                spans.add(anchor);
            } else if (extendedAnchor!=null) {
                bindpoints.add(BindPoint.of(DEFINITION,extendedAnchor, BindPoint.Type.definition));
                spans.add(extendedAnchor);
            } else {
                throw new BasicError("Unable to parse: " + template);
            }

        }
        spans.add(lastMatch >= 0 ? template.substring(lastMatch) : template);

        return new Result(spans,bindpoints);
    }

    public final static class Result {

        private final List<String> spans;
        private final List<BindPoint> bindpoints;

        public Result(List<String> spans, List<BindPoint> bindpoints) {

            this.spans = spans;
            this.bindpoints = bindpoints;
        }

        public List<String> getSpans() {
            return spans;
        }

        public List<BindPoint> getBindpoints() {
            return bindpoints;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Result result = (Result) o;
            return Objects.equals(spans, result.spans) && Objects.equals(bindpoints, result.bindpoints);
        }

        @Override
        public int hashCode() {
            return Objects.hash(spans, bindpoints);
        }

        @Override
        public String toString() {
            return "Result{" +
                "spans=" + spans +
                ", bindpoints=" + bindpoints +
                '}';
        }
    }

}
