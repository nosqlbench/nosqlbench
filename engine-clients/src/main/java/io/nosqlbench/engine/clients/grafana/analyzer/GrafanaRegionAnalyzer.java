package io.nosqlbench.engine.clients.grafana.analyzer;

import io.nosqlbench.engine.clients.grafana.GrafanaClient;
import io.nosqlbench.engine.clients.grafana.GrafanaClientConfig;
import io.nosqlbench.engine.clients.grafana.transfer.GAnnotation;
import io.nosqlbench.engine.clients.grafana.transfer.GDashboard;
import io.nosqlbench.engine.clients.grafana.transfer.GPanelDef;
import io.nosqlbench.engine.clients.grafana.transfer.GSnapshotInfo;
import io.nosqlbench.nb.api.SystemId;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GrafanaRegionAnalyzer implements Runnable {

    private String baseUrl = "";
    //    private String dashboardId = "";
    private GrafanaClient gclient;

    public static void main(String[] args) {
        GrafanaRegionAnalyzer analyzer = new GrafanaRegionAnalyzer();
        analyzer.setBaseUrl("http://44.242.139.57:3000/");
//
//
//        analyzer.setDashboardId("aIIX1f6Wz");
        analyzer.run();
    }

    private GrafanaClient getClient() {
        if (this.gclient == null) {
            GrafanaClientConfig ccfg = new GrafanaClientConfig();
            ccfg.setBaseUri(baseUrl);
            GrafanaClient newclient = new GrafanaClient(ccfg);
            Supplier<String> namer = () -> "nosqlbench-" + SystemId.getNodeId() + "-" + System.currentTimeMillis();
            newclient.cacheApiToken(namer, "Admin", Long.MAX_VALUE, Path.of("grafana_apikey"), "admin", "admin");
            this.gclient = newclient;
        }
        return this.gclient;
    }

    public void setBaseUrl(String url) {
        this.baseUrl = url;
    }

    public GSnapshotInfo createSnapshotForAnnotation(GAnnotation anno, GDashboard dashboard, String snid) {

        //session: scenario_20201215_050435_240
        //[2020-12-15T05:04:37.232Z[GMT] - 2020-12-15T05:04:37.232Z[GMT]]
        //span:interval
        //details:
        // params: ActivityDef:(4)/{keycount=5000000000L, hosts=node1, main-cycles=500, threads=1, workload=./keyvalue.yaml, cycles=2, stride=2, tags=phase:schema, password=cassandra, rf=3, pooling=16:16:500, driver=cql, rampup-cycles=5000000000, alias=keyvalue_default_schema, valuecount=5000000000L, errors=count, username=cassandra}
        //labels:
        // layer: Activity
        // alias: keyvalue_default_schema
        // driver: cql
        // workload: ./keyvalue.yaml
        // session: scenario_20201215_050435_240
        // span: interval
        // appname: nosqlbench

        //    "time": {
        //      "from": "2020-12-15T04:19:54.943Z",
        //      "raw": {
        //        "from": "2020-12-15T04:19:54.943Z",
        //        "to": "2020-12-15T15:25:11.743Z"
        //      },
        //      "to": "2020-12-15T15:25:11.743Z"
        //    },

        ZoneId zoneid = ZoneId.of("GMT");
        ZonedDateTime startTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(anno.getTime()), zoneid);
        ZonedDateTime endTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(anno.getTimeEnd()), zoneid);
        String startIsoInstant = startTime.format(DateTimeFormatter.ISO_INSTANT);
        String endIsoInstant = endTime.format(DateTimeFormatter.ISO_INSTANT);

        dashboard.getTime().setFrom(startIsoInstant);
        dashboard.getTime().setTo(endIsoInstant);

        GSnapshotInfo snapshotInfo = gclient.createSnapshot(dashboard, snid);
        return snapshotInfo;
    }

    public void getQueries(GDashboard db) {
        List<GPanelDef> graphs = db.getPanels().stream()
                .filter(p -> p.getType().equals("graph"))
                .collect(Collectors.toList());
        System.out.println(graphs.size() + " graphs...");

    }

    @Override
    public void run() {

        GrafanaClient client = getClient();

        GDashboard.fromFile("db.json");

        //List<DashboardInfo> dashboards = gclient.findDashboards();
//        GDashboardMeta dashboardMeta = client.getDashboardByUid(dashboardId);
//        List<GSnapshotInfo> snapshots = client.findSnapshots();
//
//        List<GAnnotation> annotations = client.findAnnotations(By.tag("appname:nosqlbench,layer:Activity"));
//        List<GAnnotation> mainActivityAnno = annotations.stream()
//                .filter(s -> s.getTagMap().getOrDefault("alias", "").contains("main"))
//                .filter(s -> s.getTagMap().getOrDefault("span", "").contains("interval"))
//                .sorted(Comparator.comparing(t -> t.getTime()))
//                .collect(Collectors.toList());
//
//
//        for (GAnnotation anno : mainActivityAnno) {
//            System.out.println("creating data snapshot for " + anno);
//            long start = anno.getTime();
//            long end = anno.getTimeEnd();
//            String snapshotKey = "ss-" + dashboardId + "-" + anno.getId();
//            Optional<GSnapshot> snapshot = client.findSnapshotBykeyOptionally(snapshotKey);
//            if (!snapshot.isPresent()) {
//                GSnapshotInfo info = createSnapshotForAnnotation(anno, dashboardMeta.getDashboard(), snapshotKey);
//                GSnapshot newsnapshot = client.findSnapshotBykey(info.getKey());
//            }
//        }
        System.out.println("end");
    }

}
