package io.nosqlbench.engine.core.annotation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.annotations.Annotation;
import io.nosqlbench.nb.api.annotations.Annotator;
import io.nosqlbench.nb.api.config.ConfigAware;
import io.nosqlbench.nb.api.config.ConfigLoader;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.*;

/**
 * Singleton-scoped annotator interface for the local process.
 * This uses SPI to find the annotators and some config scaffolding
 * to make configuring them easier.
 * Any number of annotators is allowed of any supporting interface.
 * Each instance of a config is used to initialize a single annotator,
 * and annotations are distributed to each of them in turn.
 */
public class Annotators {
    private final static Logger logger = LogManager.getLogger("ANNOTATORS");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static List<Annotator> annotators;

    /**
     * Initialize the active annotators. This method must be called before any others.
     *
     * @param annotatorsConfig A (possibly empty) set of annotator configurations, in any form
     *                         supported by {@link ConfigLoader}
     */
    public synchronized static void init(String annotatorsConfig) {

        ConfigLoader loader = new ConfigLoader();
        annotators = new ArrayList<>();

        LinkedHashMap<String, ServiceLoader.Provider<Annotator>> providers = getProviders();

        List<Map> configs = loader.load(annotatorsConfig, Map.class);

        if (configs != null) {

            for (Map cmap : configs) {
                Object typeObj = cmap.remove("type");
                String typename = typeObj.toString();
                ServiceLoader.Provider<Annotator> annotatorProvider = providers.get(typename);
                if (annotatorProvider == null) {
                    throw new RuntimeException("Annotation provider with selector '" + typename + "' was not found.");
                }
                Annotator annotator = annotatorProvider.get();

                if (annotator instanceof ConfigAware) {
                    ConfigAware configAware = (ConfigAware) annotator;
                    configAware.applyConfig(cmap);
                }

                annotators.add(annotator);
            }

        }

        logger.debug("Initialized " + Annotators.annotators.size() + " annotators, since the configuration is empty.");

    }

    private static List<Annotator> getAnnotators() {
        if (annotators != null) {
            return annotators;
        }
        logger.debug("Annotations are bypassed as no annotators were configured.");
        return List.of();
    }

    private synchronized static LinkedHashMap<String, ServiceLoader.Provider<Annotator>> getProviders() {
        ServiceLoader<Annotator> loader = ServiceLoader.load(Annotator.class);

        LinkedHashMap<String, ServiceLoader.Provider<Annotator>> providers;
        providers = new LinkedHashMap<>();

        loader.stream().forEach(provider -> {
            Class<? extends Annotator> type = provider.type();
            if (!type.isAnnotationPresent(Service.class)) {
                throw new RuntimeException(
                        "Annotator services must be annotated with distinct selectors\n" +
                                "such as @Service(Annotator.class,selector=\"myimpl42\")"
                );
            }
            Service service = type.getAnnotation(Service.class);
            providers.put(service.selector(), provider);
        });

        return providers;
    }

    public static synchronized void recordAnnotation(Annotation annotation) {
        for (Annotator annotator : getAnnotators()) {
            try {
                logger.trace("calling annotator " + annotator.getName());
                annotator.recordAnnotation(annotation);
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

//    public static synchronized void recordAnnotation(
//            String sessionName,
//            long startEpochMillis,
//            long endEpochMillis,
//            Map<String, String> target,
//            Map<String, String> details) {
//        getAnnotators().forEach(a -> a.recordAnnotation(sessionName, startEpochMillis, endEpochMillis, target, details));
//    }

//    public static synchronized void recordAnnotation(
//            String sessionName,
//            Map<String, String> target,
//            Map<String, String> details) {
//        recordAnnotation(sessionName, 0L, 0L, target, details);
//    }

}
