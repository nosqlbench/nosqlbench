package io.nosqlbench.engine.clients.grafana.transfer;

public class CreateSnapshotRequest {
    GDashboard dashboard;
    Long expires;
    String key;

    public CreateSnapshotRequest(GDashboard dashboard, Long expires, String key) {
        this.dashboard = dashboard;
        this.expires = expires;
        this.key = key;
    }
}
