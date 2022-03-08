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

package io.nosqlbench.nb.api.markdown.aggregator;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MutableFrontMatter extends LinkedHashMap<String,List<String>> {
    String WEIGHT = "weight";
    String TITLE = "title";

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

    public void setTitle(String title) {
        put(TITLE,List.of(title));
    }

    public void setWeight(int weight) {
        put(WEIGHT,List.of(String.valueOf(weight)));
    }

    private void assertMaxSingleValued(String fieldname) {
        if (containsKey(fieldname) && get(fieldname).size()>1) {
            throw new RuntimeException("Field '" + fieldname + "' can only have zero or one value. It is single-valued.");
        }
    }

    public String asYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        return yaml.dump(Map.of(TITLE,getTitle(),WEIGHT,getWeight()));
    }
}
