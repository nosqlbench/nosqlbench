package io.nosqlbench.nb.api.config.standard;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigSuggestions {

    public static Optional<String> getForParam(ConfigModel model, String param) {
        return suggestAlternateCase(model,param)
            .or(() -> suggestAlternates(model,param));
    }

    private static Optional<String> suggestAlternateCase(ConfigModel model, String param) {
        for (String cname : model.getNamedParams().keySet()) {
            if (cname.equalsIgnoreCase(param)) {
                return Optional.of("Did you mean '" + cname + "'?");
            }
        }
        return Optional.empty();
    }

    private static Optional<String> suggestAlternates(ConfigModel model, String param) {
        Map<Integer, Set<String>> suggestions = new HashMap<>();
        for (String candidate : model.getNamedParams().keySet()) {
            try {
                Integer distance = LevenshteinDistance.getDefaultInstance().apply(param, candidate);
                Set<String> strings = suggestions.computeIfAbsent(distance, d -> new HashSet<>());
                strings.add(candidate);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        ArrayList<Integer> params = new ArrayList<>(suggestions.keySet());
        Collections.sort(params);
        List<Set<String>> orderedSets = params.stream().map(suggestions::get).collect(Collectors.toList());
        if (orderedSets.size()==0) {
            return Optional.empty();
        } else if (orderedSets.get(0).size()==1) {
            return Optional.of("Did you mean '" + orderedSets.get(0).stream().findFirst().get() +"'?");
        } else {
            return Optional.of("Did you mean one of " + orderedSets.get(0).toString() + "?\n");
        }
    }
}
