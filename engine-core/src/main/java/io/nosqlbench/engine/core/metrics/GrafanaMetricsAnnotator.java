package io.nosqlbench.engine.core.metrics;

import io.nosqlbench.engine.clients.grafana.GrafanaClient;
import io.nosqlbench.engine.clients.grafana.transfer.Annotation;
import io.nosqlbench.nb.api.annotation.Annotator;
import io.nosqlbench.nb.api.config.ConfigAware;
import io.nosqlbench.nb.api.config.ConfigModel;
import io.nosqlbench.nb.api.config.MutableConfigModel;

import java.util.Map;
import java.util.Optional;

public class GrafanaMetricsAnnotator implements Annotator, ConfigAware {

    private final GrafanaClient client;

    public GrafanaMetricsAnnotator(String grafanaBaseUrl) {
        this.client = new GrafanaClient(grafanaBaseUrl);
    }


    @Override
    public void recordAnnotation(String sessionName, long startEpochMillis, long endEpochMillis, Map<String, String> target, Map<String, String> details) {

        Annotation annotation = new Annotation();

        // Target

        Optional.ofNullable(target.get("type"))
                .ifPresent(annotation::setType);

        long startAt = startEpochMillis > 0 ? startEpochMillis : System.currentTimeMillis();
        annotation.setTime(startAt);
        annotation.setTimeEnd(endEpochMillis > 0 ? endEpochMillis : startAt);

        String eTime = target.get("timeEnd");
        annotation.setTimeEnd((eTime != null) ? Long.valueOf(eTime) : null);

        Optional.ofNullable(target.get("id")).map(Integer::valueOf)
                .ifPresent(annotation::setId);

        Optional.ofNullable(target.get("alertId")).map(Integer::valueOf)
                .ifPresent(annotation::setAlertId);

        Optional.ofNullable(target.get("dashboardId")).map(Integer::valueOf)
                .ifPresent(annotation::setDashboardId);

        Optional.ofNullable(target.get("panelId")).map(Integer::valueOf)
                .ifPresent(annotation::setPanelId);

        Optional.ofNullable(target.get("userId")).map(Integer::valueOf)
                .ifPresent(annotation::setUserId);

        Optional.ofNullable(target.get("userName"))
                .ifPresent(annotation::setUserName);

        Optional.ofNullable(target.get("tags"))
                .ifPresent(annotation::setTags);

        Optional.ofNullable(details.get("metric"))
                .ifPresent(annotation::setMetric);

        // Details

        StringBuilder sb = new StringBuilder();
        if (details.containsKey("text")) {
            annotation.setText(details.get("text"));
        } else {
            for (String dkey : details.keySet()) {
                sb.append(sb).append(": ").append(details.get(dkey)).append("\n");
            }
            annotation.setText(details.toString());
        }

        Optional.ofNullable(details.get("data"))
                .ifPresent(annotation::setData);

        Optional.ofNullable(details.get("prevState"))
                .ifPresent(annotation::setPrevState);
        Optional.ofNullable(details.get("newState"))
                .ifPresent(annotation::setNewState);

        Annotation created = this.client.createAnnotation(annotation);
    }

    @Override
    public String getName() {
        return "grafana";
    }

    @Override
    public void applyConfig(Map<String, ?> element) {
        ConfigModel configModel = getConfigModel();


    }

    @Override
    public ConfigModel getConfigModel() {
        return new MutableConfigModel().add("baseurl", String.class).asReadOnly();
    }
}
