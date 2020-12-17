package io.nosqlbench.engine.clients.grafana.transfer;

import java.util.List;
import java.util.Map;

public class PanelDef {
    boolean collapsed;
    Map<String, String> gridPos;
    long id;
    List<PanelDef> panels;
    String description;
    Map<String, Object> fieldConfig;
    Map<String, String> options;
    String pluginVersion;
    List<Map<String, String>> targets;
    String title;
    String type;
    String datasource;


    public String toString() {
        return id + ":'" + title + "'";
    }
}
