/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.virtdata.core.templates;

import io.nosqlbench.api.errors.BasicError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BindPointParser parses a user-provide string template into spans. It builds a simple list of
 * BindPoints, and provides both the parsed spans and the BindPoints in a result.
 * Spans are provided as a list of even,(odd,even,...) string values, where there is always an odd number of entries
 * and odd-numbered entries are the text extracted from a binding. The binding values are not
 * changed from the user-specified string, and are not normalized to one of the {@link BindPoint.Type}s.
 * This is for troubleshooting and diagnostic purposes.
 * Callers will need to look at the BindPoint types to know whether a binding is provided and thus how to process it.
 */
public class BindPointParser implements BiFunction<String, Map<String, String>, BindPointParser.Result> {

    public final static Pattern BINDPOINT_ANCHOR = Pattern.compile("(\\{((?<anchor>\\w+[-_<>,\\d\\w.]*)})|(\\{\\{(?<extended>(?!}}).+?)}}))");
    public final static String DEFINITION = "DEFINITION";


    @Override
    public Result apply(String template, Map<String, String> bindings) {

        Matcher m = BINDPOINT_ANCHOR.matcher(template);
        int lastMatch = 0;
        List<String> spans = new ArrayList<>();
        List<BindPoint> bindpoints = new ArrayList<>();

        int genid=0;
        while (m.find()) {
            String pre = template.substring(lastMatch, m.start());
            spans.add(pre);
            lastMatch = m.end();

            String anchor = m.group("anchor");
            String extendedAnchor = m.group("extended");
            if (anchor != null) {
                bindpoints.add(BindPoint.of(anchor, bindings.getOrDefault(anchor, null), BindPoint.Type.reference));
                spans.add(anchor);
            } else if (extendedAnchor != null) {
                bindpoints.add(BindPoint.of(DEFINITION, extendedAnchor, BindPoint.Type.definition));
                spans.add(extendedAnchor);
            } else {
                throw new BasicError("Unable to parse: " + template);
            }

        }
        spans.add(lastMatch >= 0 ? template.substring(lastMatch) : template);

        return new Result(spans, bindpoints);
    }

    public final static class Result {

        private final List<String> spans;
        private final List<BindPoint> bindpoints;

        public Result(List<String> spans, List<BindPoint> bindpoints) {

            this.spans = spans;
            this.bindpoints = bindpoints;
        }

        /**
         * @return the spans of literal values which are between the bind points
         */
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
