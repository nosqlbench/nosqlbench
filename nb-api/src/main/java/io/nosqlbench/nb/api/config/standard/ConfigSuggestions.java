package io.nosqlbench.nb.api.config.standard;

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
