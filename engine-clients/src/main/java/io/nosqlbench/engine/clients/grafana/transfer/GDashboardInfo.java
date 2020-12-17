package io.nosqlbench.engine.clients.grafana.transfer;

import java.util.List;

public class GDashboardInfo {
    long id;
    String uid;
    String title;
    String url;
    String type;
    List<String> tags;
    boolean isStarred;

    // deprecated
    String uri;

}
