package io.nosqlbench.engine.core.annotation;

import io.nosqlbench.nb.api.annotation.Annotator;

import java.util.*;

public class Annotators {

    private static List<Annotator> annotators;
    private static Set<String> names;

    /**
     * Initialize the active annotators.
     *
     * @param annotatorsConfig A comma-separated set of annotator configs, each with optional
     *                         configuration metadata in name{config} form.
     */
    public synchronized static void init(String annotatorsConfig) {
        if (annotatorsConfig == null || annotatorsConfig.isEmpty()) {
            Annotators.names = Set.of();
        } else {

        }
        Annotators.names = names;
    }

    public synchronized static List<Annotator> getAnnotators() {
        if (names == null) {
            throw new RuntimeException("Annotators.init(...) must be called first.");
        }
        if (annotators == null) {
            annotators = new ArrayList<>();
            ServiceLoader<Annotator> loader = ServiceLoader.load(Annotator.class);
            loader.stream()
                    .map(sp -> sp.get())
                    .filter(an -> names.contains("all") || Annotators.names.contains(an.getName()))
                    .forEach(an -> {
                        annotators.add(an);
                    });
        }
        return annotators;
    }

    public static synchronized void recordAnnotation(
            String sessionName,
            long startEpochMillis,
            long endEpochMillis,
            Map<String, String> target,
            Map<String, String> details) {
        getAnnotators().forEach(a -> a.recordAnnotation(sessionName, startEpochMillis, endEpochMillis, target, details));
    }

    public static synchronized void recordAnnotation(
            String sessionName,
            Map<String, String> target,
            Map<String, String> details) {
        recordAnnotation(sessionName, 0L, 0L, target, details);
    }

}
