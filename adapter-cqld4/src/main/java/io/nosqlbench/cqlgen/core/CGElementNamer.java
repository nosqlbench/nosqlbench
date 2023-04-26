/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.cqlgen.core;

import io.nosqlbench.api.config.NBLabeledElement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CGElementNamer implements Function<Map<String, String>, String> {

    public static final String _DEFAULT_TEMPLATE = "[PREFIX-][OPTYPE-][KEYSPACE__][TABLE][-DATATYPE]";

    // for convenient reference
    public static final String PREFIX = "PREFIX";
    public static final String OPTYPE = "OPTYPE";
    public static final String KEYSPACE = "KEYSPACE";
    public static final String TABLE = "TABLE";
    public static final String DATATYPE = "DATATYPE";

    private final List<Section> sections = new ArrayList<>();
    private final String spec;
    private final List<Function<String, String>> transformers = new ArrayList<>();

    public CGElementNamer(final String template, final List<Function<String,String>> transformers) {
        spec = template;
        this.transformers.addAll(transformers);
        final Pattern pattern = Pattern.compile("(?<prefix>[^\\]]+)?\\[(?<section>(?<pre>.*?)(?<name>[A-Z]+)(?<required>!)?(?<post>.*?))?]");
        final Matcher scanner = pattern.matcher(template);
        while (scanner.find()) {
            if (null != scanner.group("prefix")) {
                final String prefix = scanner.group("prefix");
                this.sections.add(new Section(null, prefix, true));
            }
            if (null != scanner.group("section")) {
                final Section section = new Section(
                    scanner.group("name").toLowerCase(),
                    scanner.group("pre") +
                        scanner.group("name")
                        + scanner.group("post"),
                    null != scanner.group("required"));
                this.sections.add(section);
            }
        }
    }

    public CGElementNamer(final String template) {
        this(template, List.of());
    }

    public CGElementNamer() {
        this(CGElementNamer._DEFAULT_TEMPLATE, List.of());
    }

    /**
     * For each section in the Element Namer's ordered templates,
     * if the labels contain a value for it, substitute the value
     * for the named label into the section where the field is named in upper-case,
     * including all the surrounding non-character literals.
     *
     * @param labels Metadata for the element to be named.
     * @return A formatted string, with the sections added which are defined.
     */
    @Override
    public String apply(final Map<String, String> labels) {
        final StringBuilder sb = new StringBuilder();
        for (final Section section : this.sections) {
            final String appender = section.apply(labels);
            sb.append(appender);
        }
        String value = sb.toString();
        for (final Function<String, String> transformer : this.transformers) value = transformer.apply(value);
        return value;
    }

    public String apply(final NBLabeledElement element, final String... keysAndValues) {
        final LinkedHashMap<String, String> mylabels = new LinkedHashMap<>();
        for (int idx = 0; idx < keysAndValues.length; idx += 2)
            mylabels.put(keysAndValues[idx], keysAndValues[idx + 1]);
        mylabels.putAll(element.getLabels());
        return this.apply(mylabels);
    }

    private static final class Section implements Function<Map<String, String>, String> {
        String name;
        String template;
        boolean required;

        public Section(final String name, final String template, final boolean required) {
            this.name = (null != name) ? name.toLowerCase() : null;
            this.template = template.toLowerCase();
            this.required = required;
        }

        @Override
        public String apply(final Map<String, String> labels) {
            if (null == name) return this.template;
            if (labels.containsKey(this.name)) return this.template.replace(this.name, labels.get(this.name));
            if (labels.containsKey(this.name.toUpperCase()))
                return this.template.replace(this.name, labels.get(this.name.toUpperCase()));
            if (this.required)
                throw new RuntimeException("Section label '" + this.name + "' was not provided for template, but it is required.");
            return "";
        }

        @Override
        public String toString() {
            return "Section{" +
                "name='" + this.name + '\'' +
                ", template='" + this.template + '\'' +
                ", required=" + this.required +
                '}';
        }
    }

    @Override
    public String toString() {
        return "ElementNamer: " + spec + "]\n" +
            "sections=" + this.sections +
            '}';
    }
}
