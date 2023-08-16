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

package io.nosqlbench.engine.api.util;

import io.nosqlbench.engine.api.activityapi.core.Activity;


import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfigTuples implements Iterable<ConfigTuples.Section> {

    List<Section> sections;

    public ConfigTuples(String configdata) {
        this.sections = parseParams(configdata);
    }
    private ConfigTuples(List<Section> sections) {
        this.sections = sections;
    }

    public ConfigTuples(Activity activity, String param) {
        this(activity.getParams().getOptionalString(param).orElse(""));
    }

    private List<Section> parseParams(String configdata) {
        try {
            List<Section> sections = Arrays.stream(configdata.split("[,]"))
                    .filter(Objects::nonNull)
                    .filter(s -> !s.isEmpty())
                    .map(s -> new Section(s, "[:=]"))
                    .collect(Collectors.toList());
            return sections;
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse params: '" + configdata + "': " + e);
        }
    }

    public Optional<String> getStringOfFirst(String name) {
        return sections.stream().filter(s->s.isNamed(name)).findFirst().map(s->s.get(1));
    }

    public Optional<Integer> getIntOfFirst(String name) {
        return getStringOfFirst(name).map(Integer::valueOf);
    }

    public Optional<Long> getLongOfFirst(String name) {
        return getStringOfFirst(name).map(Long::valueOf);
    }

    public Optional<Double> getDoubleOfFirst(String name) {
        return getStringOfFirst(name).map(Double::valueOf);
    }

    public ConfigTuples getAllMatching(String... names) {
        List<Pattern> patterns = Arrays.asList(names).stream().map(Pattern::compile).collect(Collectors.toList());

        List<Section> matching = sections
                .stream()
                .filter(s -> patterns.stream().anyMatch(p -> s.get(0).matches(p.pattern())))
                .collect(Collectors.toList());
        return new ConfigTuples(matching);
    }


    @Override
    public Iterator<Section> iterator() {
        return sections.iterator();
    }

    public Section get(int i) {
        return sections.get(i);
    }

    public static class Section {
        public final String[] data;

        public Section(String data, String delim) {
            this.data = data.split(delim);
        }

        public boolean isNamed(String name) {
            return (data.length>0 && data[0].equals(name));
        }

        public String get(int i) {
            return data[i];
        }

        public String toString() {
            return Arrays.toString(data);
        }
    }

}
