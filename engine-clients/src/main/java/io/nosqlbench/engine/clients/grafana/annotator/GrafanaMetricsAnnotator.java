package io.nosqlbench.engine.clients.grafana.annotator;

import io.nosqlbench.engine.clients.grafana.GrafanaClient;
import io.nosqlbench.engine.clients.grafana.GrafanaClientConfig;
import io.nosqlbench.engine.clients.grafana.transfer.GAnnotation;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.OnError;
import io.nosqlbench.nb.api.SystemId;
import io.nosqlbench.nb.api.annotations.Annotation;
import io.nosqlbench.nb.api.annotations.Annotator;
import io.nosqlbench.nb.api.config.params.ParamsParser;
import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigurable;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Service(value = Annotator.class, selector = "grafana")
public class GrafanaMetricsAnnotator implements Annotator, NBConfigurable {

    private final static Logger logger = LogManager.getLogger("ANNOTATORS");
    //private final static Logger annotationsLog = LogManager.getLogger("ANNOTATIONS" );
    private OnError onError = OnError.Warn;

    private GrafanaClient client;
    private Map<String, String> tags = new LinkedHashMap<>();

    public GrafanaMetricsAnnotator() {
    }

    @Override
    public void recordAnnotation(Annotation annotation) {
        try {
            GAnnotation ga = new GAnnotation();

            ga.setTime(annotation.getStart());
            ga.setTimeEnd(annotation.getEnd());

            annotation.getLabels().forEach((k, v) -> {
                ga.getTags().add(k + ":" + v);
            });
            ga.getTags().add("layer:" + annotation.getLayer().toString());

            if (annotation.getStart() == annotation.getEnd()) {
                ga.getTags().add("span:instant");
            } else {
                ga.getTags().add("span:interval");
            }

            Map<String, String> labels = annotation.getLabels();

            Optional.ofNullable(labels.get("alertId"))
                .map(Integer::parseInt).ifPresent(ga::setAlertId);

            ga.setText(annotation.toString());

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

            GAnnotation created = this.client.createAnnotation(ga);

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
    public void applyConfig(NBConfiguration cfg) {

        GrafanaClientConfig gc = new GrafanaClientConfig();
        gc.setBaseUri(cfg.param("baseurl", String.class));

        cfg.getOptional("tags")
            .map(t -> ParamsParser.parse(t, false))
            .ifPresent(this::setTags);


        cfg.getOptional("username")
            .ifPresent(
                username ->
                    gc.basicAuth(
                        username,
                        cfg.getOptional("password").orElse("")
                    )
            );


        Path keyfilePath = null;
        Optional<String> optionalApikeyfile = cfg.getEnvOptional("apikeyfile");
        Optional<String> optionalApikey = cfg.getOptional("apikey");

        if (optionalApikeyfile.isPresent()) {
            keyfilePath=optionalApikeyfile.map(Path::of).orElseThrow();
        } else if (optionalApikey.isPresent()) {
            gc.addHeaderSource(() -> Map.of("Authorization", "Bearer " + optionalApikey.get()));
        } else {
            throw new BasicError("Undefined keyfile parameters.");
        }

        cfg.getOptional("onerror").map(OnError::valueOfName).ifPresent(this::setOnError);

        this.client = new GrafanaClient(gc);

        String keyName = "nosqlbench-" + SystemId.getNodeId() + "-" + System.currentTimeMillis();
        Supplier<String> namer = () -> "nosqlbench-" + SystemId.getNodeId() + "-" + System.currentTimeMillis();
        this.client.cacheApiToken(namer, "Admin", Long.MAX_VALUE, keyfilePath, "admin", "admin");

    }

    private void setOnError(OnError onError) {
        this.onError=onError;
    }

    private void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(this.getClass())
            .required("baseurl", String.class,
                "The base url of the grafana node, like http://localhost:3000/")
            .defaults("apikeyfile", "$NBSTATEDIR/grafana/grafana_apikey",
                "The file that contains the api key, supersedes apikey")
            .optional("apikey", String.class,
                "The api key to use, supersedes basic username and password")
            .optional("username", String.class,
                "The username to use for basic auth")
            .optional("password", String.class,
                "The password to use for basic auth")
            .defaults("tags", "source:nosqlbench",
                "The tags that identify the annotations, in k:v,... form")
            .defaults("onerror", "warn",
                "What to do when an error occurs while posting an annotation")
            .defaults("timeoutms", 5000,
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
