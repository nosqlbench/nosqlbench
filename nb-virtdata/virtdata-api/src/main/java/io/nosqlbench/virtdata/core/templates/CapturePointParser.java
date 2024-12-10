/*
 * Copyright (c) nosqlbench
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
import java.util.stream.Collectors;

/// A capture point is named variable which is to be extracted from the
/// result of an operation, using whichever syntax and type conventions
/// that are appropriate to the specific op implementation.
/// The details are discussed in [NB Field Capture
/// Discussion](https://github.com/nosqlbench/nosqlbench/discussions/938)
///
/// Examples:
/// * capture the value of "field42" (into variable "field42"): `select \[field42],* from ...`
/// * capture the value of "field42" into variable "getitgotitgood": `select \[field42 as
/// getitgotitgood],* from ...`
///
/// Valid identifiers consist of any word character followed by zero or more word characters,
/// digits, hyphens,
/// underscores, or periods. This applies to both the field name to capture and the variable names
/// to capture
/// their values in. Alternately, the `*` character may be used to indicate that all available
/// fields should be captured.
///
/// Optional type assertions are allowed which will ensure that the value can be assigned or
/// otherwise coerced
/// into the expected value:
/// * `select \[(List) field42] ...`
/// * `select \[(List<Number>)field42)] ...`
/// * `select \[(int[]) field42)] ...`
/// * `select \[(java.lang.Number) field42]`
/// * `select \[(Map) field42] ...`
///
/// For details on how capture points are used at runtime, consult [[DynamicVariableCapture]]
public class CapturePointParser implements Function<String, CapturePointParser.Result> {

    public final static Pattern CAPTUREPOINT_PATTERN = Pattern.compile(
        "(\\[(\\((?<cast>[^)]+)\\))? *(?<capture>\\w+[-_\\d\\w.]*|\\*)(\\s+[aA][sS]\\s+" +
            "(?<alias>\\w+[-_\\d\\w.]*))?])");

    @Override
    public Result apply(String template) {
        StringBuilder raw = new StringBuilder();
        Matcher m = CAPTUREPOINT_PATTERN.matcher(template);
        List<CapturePoint> captures = new ArrayList<>();

        while (m.find()) {
            CapturePoint captured = CapturePoint.of(
                m.group("cast"), m.group("capture"), m.group("alias"));
            captures.add(captured);
            m.appendReplacement(raw, captured.getSourceName());
        }
        m.appendTail(raw);

        return new Result(raw.toString(), new CapturePoints(captures));
    }

    public Result parse(java.util.Map<?, ?> rawdata) {
        String specFromMap = rawdata.keySet().stream().map(
            ko -> "[" + rawdata.get(ko) + " as " + ko.toString() + "]").collect(
            Collectors.joining(""));
        return apply(specFromMap);
    }

    public final static record Result
        (String rawTemplate, CapturePoints captures) {

        public String getRawTemplate() {
            return this.rawTemplate;
        }

        public List<CapturePoint> getCaptures() {
            return this.captures;
        }

        @Override
        public int hashCode() {
            return Objects.hash(rawTemplate, captures);
        }

        @Override
        public String toString() {
            return "Result{" + "rawTemplate='" + rawTemplate + '\'' + ", captures=" + captures + '}';
        }
    }
}
