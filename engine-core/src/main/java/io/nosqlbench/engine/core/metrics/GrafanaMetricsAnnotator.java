package io.nosqlbench.engine.core.metrics;

import io.nosqlbench.engine.clients.grafana.GrafanaClient;
import io.nosqlbench.engine.clients.grafana.GrafanaClientConfig;
import io.nosqlbench.engine.clients.grafana.transfer.GrafanaAnnotation;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.annotations.Annotation;
import io.nosqlbench.nb.api.annotations.Annotator;
import io.nosqlbench.nb.api.config.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Service(value = Annotator.class, selector = "grafana")
public class GrafanaMetricsAnnotator implements Annotator, ConfigAware {

    private final static Logger logger = LogManager.getLogger("ANNOTATORS");
    private final static Logger annotationsLog = LogManager.getLogger("ANNOTATIONS");
    private OnError onError = OnError.Warn;

    private GrafanaClient client;
    private Map<String, String> tags = new LinkedHashMap<>();

    public GrafanaMetricsAnnotator() {
    }

    @Override
    public void recordAnnotation(Annotation annotation) {
        try {
            GrafanaAnnotation ga = new GrafanaAnnotation();

            ga.setTime(annotation.getStart());
            ga.setTimeEnd(annotation.getEnd());

            annotation.getLabels().forEach((k, v) -> {
                ga.getTags().put(k, v);
            });
            ga.getTags().put("layer", annotation.getLayer().toString());

            Map<String, String> labels = annotation.getLabels();

            Optional.ofNullable(labels.get("alertId"))
                    .map(Integer::parseInt).ifPresent(ga::setAlertId);

            ga.setData(annotation.toString());

            annotation.getSession();


            // Target
            Optional.ofNullable(labels.get("type"))
                    .ifPresent(ga::setType);

            Optional.ofNullable(labels.get("id")).map(Integer::valueOf)
                    .ifPresent(ga::setId);

            Optional.ofNullable(labels.get("alertId")).map(Integer::valueOf)
                    .ifPresent(ga::setAlertId);

            Optional.ofNullable(labels.get("dashboardId")).map(Integer::valueOf)
                    .ifPresent(ga::setDashboardId);

            Optional.ofNullable(labels.get("panelId")).map(Integer::valueOf)
                    .ifPresent(ga::setPanelId);

            Optional.ofNullable(labels.get("userId")).map(Integer::valueOf)
                    .ifPresent(ga::setUserId);

            Optional.ofNullable(labels.get("userName"))
                    .ifPresent(ga::setUserName);

            Optional.ofNullable(labels.get("metric"))
                    .ifPresent(ga::setMetric);

            // Details

            annotationsLog.info("ANNOTATION:" + ga.toString());
            GrafanaAnnotation created = this.client.createAnnotation(ga);

        } catch (Exception e) {
            switch (onError) {
                case Warn:
                    logger.warn("Error while reporting annotation: " + e.getMessage(), e);
                    break;
                case Throw:
                    throw e;
            }
        }

    }

    @Override
    public String getName() {
        return "grafana";
    }

    @Override
    public void applyConfig(Map<String, ?> providedConfig) {
        ConfigModel configModel = getConfigModel();
        ConfigReader cfg = configModel.apply(providedConfig);

        GrafanaClientConfig gc = new GrafanaClientConfig();
        gc.setBaseUri(cfg.param("baseurl", String.class));

        if (cfg.containsKey("tags")) {
            this.tags = ParamsParser.parse(cfg.param("tags", String.class), false);
        }

        if (cfg.containsKey("apikeyfile")) {
            String apikeyfile = cfg.paramEnv("apikeyfile", String.class);
            AuthWrapper authHeaderSupplier = new AuthWrapper(
                    "Authorization",
                    new GrafanaKeyFileReader(apikeyfile),
                    s -> "Bearer " + s + ";"
            );
            gc.addHeaderSource(authHeaderSupplier);
        }

        if (cfg.containsKey("apikey")) {
            gc.addHeaderSource(() -> Map.of("Authorization", "Bearer " + cfg.param("apikey", String.class)));
        }

        if (cfg.containsKey("username")) {
            if (cfg.containsKey("password")) {
                gc.basicAuth(
                        cfg.param("username", String.class),
                        cfg.param("password", String.class)
                );
            } else {
                gc.basicAuth(cfg.param("username", String.class), "");
            }
        }

        this.onError = OnError.valueOfName(cfg.get("onerror").toString());

        this.client = new GrafanaClient(gc);
    }

    @Override
    public ConfigModel getConfigModel() {
        return new MutableConfigModel(this)
                .required("baseurl", String.class,
                        "The base url of the grafana node, like http://localhost:3000/")
                .defaultto("apikeyfile", "$NBSTATEDIR/grafana_key",
                        "The file that contains the api key, supersedes apikey")
                .optional("apikey", String.class,
                        "The api key to use, supersedes basic username and password")
                .optional("username", String.class,
                        "The username to use for basic auth")
                .optional("password", String.class,
                        "The password to use for basic auth")
                .defaultto("tags", "source:nosqlbench",
                        "The tags that identify the annotations, in k:v,... form")
//                .defaultto("onerror", OnError.Warn)
                .defaultto("onerror", "warn",
                        "What to do when an error occurs while posting an annotation")
                .defaultto("timeoutms", 5000,
                        "connect and transport timeout for the HTTP client")
                .asReadOnly();
    }


    public static class AuthWrapper implements Supplier<Map<String, String>> {

        private final Function<String, String> valueMapper;
        private final String headerName;
        private final Supplier<String> valueSupplier;

        public AuthWrapper(String headerName, Supplier<String> valueSupplier, Function<String, String> valueMapper) {
            this.headerName = headerName;
            this.valueSupplier = valueSupplier;
            this.valueMapper = valueMapper;
        }

        @Override
        public Map<String, String> get() {
            String value = valueSupplier.get();
            if (value != null) {
                value = valueMapper.apply(value);
                return Map.of(headerName, value);
            }
            return Map.of();
        }
    }
}
