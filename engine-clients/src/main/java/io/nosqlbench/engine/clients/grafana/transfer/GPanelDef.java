package io.nosqlbench.engine.clients.grafana.transfer;

import java.util.List;
import java.util.Map;

public class GPanelDef {
    boolean collapsed;
    Map<String, String> gridPos;
    long id;
    List<GPanelDef> panels;
    String description;
    Map<String, Object> fieldConfig;
    Map<String, String> options;
    String pluginVersion;
    List<GTarget> targets;
    String title;
    String type;
    String datasource;


    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    public Map<String, String> getGridPos() {
        return gridPos;
    }

    public void setGridPos(Map<String, String> gridPos) {
        this.gridPos = gridPos;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<GPanelDef> getPanels() {
        return panels;
    }

    public void setPanels(List<GPanelDef> panels) {
        this.panels = panels;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getFieldConfig() {
        return fieldConfig;
    }

    public void setFieldConfig(Map<String, Object> fieldConfig) {
        this.fieldConfig = fieldConfig;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public List<GTarget> getTargets() {
        return targets;
    }

    public void setTargets(List<GTarget> targets) {
        this.targets = targets;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String toString() {
        return id + ":'" + title + "'";
    }

    public static class GTarget {
        String queryType;
        String refId;
        String expr;
        String interval;
        String legendFormat;
        boolean hide;

        public String getQueryType() {
            return queryType;
        }

        public void setQueryType(String queryType) {
            this.queryType = queryType;
        }

        public String getRefId() {
            return refId;
        }

        public void setRefId(String refId) {
            this.refId = refId;
        }

        public String getExpr() {
            return expr;
        }

        public void setExpr(String expr) {
            this.expr = expr;
        }

        public String getInterval() {
            return interval;
        }

        public void setInterval(String interval) {
            this.interval = interval;
        }

        public String getLegendFormat() {
            return legendFormat;
        }

        public void setLegendFormat(String legendFormat) {
            this.legendFormat = legendFormat;
        }

        public boolean isHide() {
            return hide;
        }

        public void setHide(boolean hide) {
            this.hide = hide;
        }

        @Override
        public String toString() {
            return "GTarget{" +
                    "queryType='" + queryType + '\'' +
                    ", refId='" + refId + '\'' +
                    ", expr='" + expr + '\'' +
                    ", interval='" + interval + '\'' +
                    ", legendFormat='" + legendFormat + '\'' +
                    ", hide=" + hide +
                    '}';
        }
    }
}
