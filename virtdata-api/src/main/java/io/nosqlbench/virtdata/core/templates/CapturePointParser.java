package io.nosqlbench.virtdata.core.templates;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CapturePointParser implements Function<String, CapturePointParser.Result> {

    public final static Pattern CAPTUREPOINT_PATTERN = Pattern.compile(
        "(\\[(?<capture>\\w+[-_\\d\\w.]*)(\\s+[aA][sS]\\s+(?<alias>\\w+[-_\\d\\w.]*))?])"
    );
    @Override
    public Result apply(String template) {
        StringBuilder raw = new StringBuilder();
        Matcher m = CAPTUREPOINT_PATTERN.matcher(template);
        List<CapturePoint> captures = new ArrayList<>();

        while (m.find()) {
            CapturePoint captured = CapturePoint.of(m.group("capture"), m.group("alias"));
            captures.add(captured);
            m.appendReplacement(raw,captured.getName());
        }
        m.appendTail(raw);

        return new Result(raw.toString(),captures);
    }

    public final static class Result {

        private final String rawTemplate;
        private final List<CapturePoint> captures;

        public Result(String rawTemplate, List<CapturePoint> captures) {

            this.rawTemplate = rawTemplate;
            this.captures = captures;
        }

        public String getRawTemplate() {
            return this.rawTemplate;
        }

        public List<CapturePoint> getCaptures() {
            return this.captures;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Result result = (Result) o;
            return Objects.equals(rawTemplate, result.rawTemplate) && Objects.equals(captures, result.captures);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rawTemplate, captures);
        }

        @Override
        public String toString() {
            return "Result{" +
                "rawTemplate='" + rawTemplate + '\'' +
                ", captures=" + captures +
                '}';
        }
    }
}
