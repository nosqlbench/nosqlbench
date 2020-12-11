package io.nosqlbench.engine.clients.grafana.transfer;

import java.util.HashMap;
import java.util.Map;

public class DashboardResponse {
    Map<String, String> meta = new HashMap<>();
    Dashboard dashboard;
}
