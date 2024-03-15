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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CapturePointParser implements Function<String, CapturePointParser.ParsedCapturePoint> {

    public final static Pattern CAPTURE_PARAM_PATTERN = Pattern.compile(
        "(\\s*(?<param>as|scope|type)\\s*:\\s*(?<value>[-_.a-zA-Z0-9<>]+))"
    );
    public final static Pattern CAPTURE_POINT_PATTERN = Pattern.compile(
        "\\[\\s*(?<name>[a-z][_a-zA-Z0-9]*)(?<params>" + CAPTURE_PARAM_PATTERN.pattern() + "+)*]"
    );

    @Override
    public ParsedCapturePoint apply(String template) {
        StringBuilder raw = new StringBuilder();
        Matcher m = CAPTURE_POINT_PATTERN.matcher(template);
        List<io.nosqlbench.virtdata.core.templates.CapturePoint> captures = new ArrayList<>();

        while (m.find()) {
            String captureName = m.group("name");
            String captureParams = m.group("params");
            String storedName = captureName;
            Class<?> storedClass = Object.class;
            Class<?> elementType = null;
            io.nosqlbench.virtdata.core.templates.CapturePoint.Scope storedScope = io.nosqlbench.virtdata.core.templates.CapturePoint.Scope.stanza;

            if (captureParams != null) {
                Matcher pfinder = CAPTURE_PARAM_PATTERN.matcher(captureParams);
                while (pfinder.find()) {
                    String param = pfinder.group("param");
                    String value = pfinder.group("value");
                    switch (param) {
                        case "as":
                            storedName = value;
                            break;
                        case "scope":
                            storedScope = io.nosqlbench.virtdata.core.templates.CapturePoint.Scope.valueOf(value);
                            break;
                        case "type":
                            try {
                                storedClass = Class.forName(value);
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                    }
                }
            }
            io.nosqlbench.virtdata.core.templates.CapturePoint captured =
                io.nosqlbench.virtdata.core.templates.CapturePoint.of(captureName, storedName, storedScope, storedClass,
                    null);
            captures.add(captured);
            m.appendReplacement(raw, captured.getCapturedName());


        }
        m.appendTail(raw);

        return new ParsedCapturePoint(raw.toString(), captures);
    }

    public final static class ParsedCapturePoint {

        private final String rawTemplate;
        private final List<io.nosqlbench.virtdata.core.templates.CapturePoint> captures;

        public ParsedCapturePoint(String rawTemplate, List<io.nosqlbench.virtdata.core.templates.CapturePoint> captures) {
            this.rawTemplate = rawTemplate;
            this.captures = captures;
        }

        public String getRawTemplate() {
            return this.rawTemplate;
        }

        public List<io.nosqlbench.virtdata.core.templates.CapturePoint> getCaptures() {
            return this.captures;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParsedCapturePoint capturePoint = (ParsedCapturePoint) o;
            return Objects.equals(rawTemplate, capturePoint.rawTemplate) && Objects.equals(captures, capturePoint.captures);
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
