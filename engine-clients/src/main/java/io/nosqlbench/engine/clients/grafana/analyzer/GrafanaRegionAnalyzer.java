package io.nosqlbench.engine.clients.grafana.analyzer;

import io.nosqlbench.engine.clients.grafana.By;
import io.nosqlbench.engine.clients.grafana.GrafanaClient;
import io.nosqlbench.engine.clients.grafana.GrafanaClientConfig;
import io.nosqlbench.engine.clients.grafana.transfer.Annotations;
import io.nosqlbench.engine.clients.grafana.transfer.DashboardResponse;
import io.nosqlbench.engine.clients.grafana.transfer.GrafanaAnnotation;
import io.nosqlbench.nb.api.SystemId;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GrafanaRegionAnalyzer {

    public static void main(String[] args) {

        GrafanaClientConfig ccfg = new GrafanaClientConfig();
        ccfg.setBaseUri("http://18.191.247.162:3000/");
        GrafanaClient gclient = new GrafanaClient(ccfg);
        Supplier<String> namer = () -> "nosqlbench-" + SystemId.getNodeId() + "-" + System.currentTimeMillis();
        gclient.cacheApiToken(namer, "Admin", Long.MAX_VALUE, Path.of("grafana_apikey"), "admin", "admin");


        DashboardResponse dashboardResponse = gclient.getDashboardByUid("aIIX1f6Wz");


        Annotations annotations = gclient.findAnnotations(By.tag("appname:nosqlbench,layer:Activity"));
        List<GrafanaAnnotation> mainActivities = annotations.stream()
                .filter(s -> s.getTagMap().getOrDefault("alias", "").contains("main"))
                .sorted(Comparator.comparing(t -> t.getTime()))
                .collect(Collectors.toList());
        System.out.println("end");
        GrafanaAnnotation last = mainActivities.get(mainActivities.size() - 1);
        long start = last.getTime();
        long end = last.getTimeEnd();

    }
}
