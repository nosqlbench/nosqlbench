package io.nosqlbench.engine.clients.grafana.transfer;

public class GDashboardMeta {

    GMeta meta;
    GDashboard dashboard;

    public GMeta getMeta() {
        return meta;
    }

    public void setMeta(GMeta meta) {
        this.meta = meta;
    }

    public GDashboard getDashboard() {
        return dashboard;
    }

    public void setDashboard(GDashboard dashboard) {
        this.dashboard = dashboard;
    }
}
