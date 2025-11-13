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

package io.nosqlbench.nb.api.markdown.aggregator;

import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MutableFrontMatter extends LinkedHashMap<String,List<String>> {
    String WEIGHT = "weight";
    String TITLE = "title";
    String SOURCE = "source";
    String DESCRIPTION = "description";
    String TEMPLATE = "template";
    String DATE = "date";
    String QUADRANT = "quadrant";
    String TOPIC = "topic";
    String CATEGORY = "category";
    String TAGS = "tags";
    String TESTABLE = "testable";
    String AUTHOR = "author";
    String GENERATOR = "generator";

    MutableFrontMatter(Map<String,List<String>> data) {
        this.putAll(data);
    }

    public String getTitle() {
        assertMaxSingleValued(TITLE);
        return Optional.ofNullable(get(TITLE)).map(l -> l.get(0)).orElse(null);
    }

    public int getWeight() {
        assertMaxSingleValued(WEIGHT);
        return Optional.ofNullable(get(WEIGHT)).map(l -> l.get(0)).map(Integer::parseInt).orElse(0);
    }

    public String getSource() {
        assertMaxSingleValued(SOURCE);
        return Optional.ofNullable(get(SOURCE)).map(l -> l.get(0)).orElse(null);
    }

    public void setTitle(String title) {
        put(TITLE,List.of(title));
    }

    public void setWeight(long weight) {
        put(WEIGHT,List.of(String.valueOf(weight)));
    }

    public void setSource(String source) {
        put(SOURCE,List.of(source));
    }

    public String getDescription() {
        assertMaxSingleValued(DESCRIPTION);
        return Optional.ofNullable(get(DESCRIPTION)).map(l -> l.get(0)).orElse(null);
    }

    public void setDescription(String description) {
        put(DESCRIPTION,List.of(description));
    }

    public String getTemplate() {
        assertMaxSingleValued(TEMPLATE);
        return Optional.ofNullable(get(TEMPLATE)).map(l -> l.get(0)).orElse(null);
    }

    public void setTemplate(String template) {
        put(TEMPLATE,List.of(template));
    }

    public String getDate() {
        assertMaxSingleValued(DATE);
        return Optional.ofNullable(get(DATE)).map(l -> l.get(0)).orElse(null);
    }

    public void setDate(String date) {
        put(DATE,List.of(date));
    }

    public String getQuadrant() {
        assertMaxSingleValued(QUADRANT);
        return Optional.ofNullable(get(QUADRANT)).map(l -> l.get(0)).orElse(null);
    }

    public void setQuadrant(String quadrant) {
        put(QUADRANT,List.of(quadrant));
    }

    public String getTopic() {
        assertMaxSingleValued(TOPIC);
        return Optional.ofNullable(get(TOPIC)).map(l -> l.get(0)).orElse(null);
    }

    public void setTopic(String topic) {
        put(TOPIC,List.of(topic));
    }

    public String getCategory() {
        assertMaxSingleValued(CATEGORY);
        return Optional.ofNullable(get(CATEGORY)).map(l -> l.get(0)).orElse(null);
    }

    public void setCategory(String category) {
        put(CATEGORY,List.of(category));
    }

    public List<String> getTags() {
        return Optional.ofNullable(get(TAGS)).orElse(new ArrayList<>());
    }

    public void setTags(List<String> tags) {
        put(TAGS,tags);
    }

    public void addTag(String tag) {
        if (!containsKey(TAGS)) {
            put(TAGS,new ArrayList<>());
        }
        get(TAGS).add(tag);
    }

    public boolean isTestable() {
        assertMaxSingleValued(TESTABLE);
        return Optional.ofNullable(get(TESTABLE)).map(l -> l.get(0)).map(Boolean::parseBoolean).orElse(false);
    }

    public void setTestable(boolean testable) {
        put(TESTABLE,List.of(String.valueOf(testable)));
    }

    public String getAuthor() {
        assertMaxSingleValued(AUTHOR);
        return Optional.ofNullable(get(AUTHOR)).map(l -> l.get(0)).orElse(null);
    }

    public void setAuthor(String author) {
        put(AUTHOR,List.of(author));
    }

    public String getGenerator() {
        assertMaxSingleValued(GENERATOR);
        return Optional.ofNullable(get(GENERATOR)).map(l -> l.get(0)).orElse(null);
    }

    public void setGenerator(String generator) {
        put(GENERATOR,List.of(generator));
    }

    private void assertMaxSingleValued(String fieldname) {
        if (containsKey(fieldname) && get(fieldname).size()>1) {
            throw new RuntimeException("Field '" + fieldname + "' can only have zero or one value. It is single-valued.");
        }
    }

    public String asYaml() {
        DumpSettings settings = DumpSettings.builder().setDefaultFlowStyle(FlowStyle.BLOCK).build();
        Dump dump = new Dump(settings);
        return dump.dumpToString(Map.of(TITLE,getTitle(),WEIGHT,getWeight(),SOURCE,getSource()));

    }

    /**
     * Export front matter as TOML format with +++ delimiters for Zola static site generator.
     * Only includes fields that have been set (non-null values).
     *
     * @return TOML-formatted front matter string
     */
    public String asToml() {
        StringBuilder toml = new StringBuilder();
        toml.append("+++\n");

        // Required fields
        if (getTitle() != null) {
            toml.append("title = \"").append(escapeToml(getTitle())).append("\"\n");
        }

        // Optional top-level fields
        if (getDescription() != null) {
            toml.append("description = \"").append(escapeToml(getDescription())).append("\"\n");
        }

        if (getWeight() > 0) {
            toml.append("weight = ").append(getWeight()).append("\n");
        }

        if (getTemplate() != null) {
            toml.append("template = \"").append(escapeToml(getTemplate())).append("\"\n");
        }

        if (getDate() != null) {
            toml.append("date = ").append(getDate()).append("\n");
        }

        // [extra] section for compositional metadata
        boolean hasExtra = getQuadrant() != null || getTopic() != null || getCategory() != null
                        || !getTags().isEmpty() || isTestable() || getAuthor() != null
                        || getGenerator() != null || getSource() != null;

        if (hasExtra) {
            toml.append("\n[extra]\n");

            if (getQuadrant() != null) {
                toml.append("quadrant = \"").append(escapeToml(getQuadrant())).append("\"\n");
            }

            if (getTopic() != null) {
                toml.append("topic = \"").append(escapeToml(getTopic())).append("\"\n");
            }

            if (getCategory() != null) {
                toml.append("category = \"").append(escapeToml(getCategory())).append("\"\n");
            }

            if (!getTags().isEmpty()) {
                String tagsStr = getTags().stream()
                    .map(tag -> "\"" + escapeToml(tag) + "\"")
                    .collect(Collectors.joining(", "));
                toml.append("tags = [").append(tagsStr).append("]\n");
            }

            if (isTestable()) {
                toml.append("testable = true\n");
            }

            if (getAuthor() != null) {
                toml.append("author = \"").append(escapeToml(getAuthor())).append("\"\n");
            }

            if (getGenerator() != null) {
                toml.append("generator = \"").append(escapeToml(getGenerator())).append("\"\n");
            }

            if (getSource() != null) {
                toml.append("source = \"").append(escapeToml(getSource())).append("\"\n");
            }
        }

        toml.append("+++\n");
        return toml.toString();
    }

    /**
     * Escape special characters for TOML string values.
     */
    private String escapeToml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

}
