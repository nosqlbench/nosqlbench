/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.engine.clients.grafana.analyzer;

import io.nosqlbench.nb.api.metadata.SystemId;
import io.nosqlbench.engine.clients.grafana.GRangeResult;
import io.nosqlbench.engine.clients.grafana.GStitcher;
import io.nosqlbench.engine.clients.grafana.GrafanaClient;
import io.nosqlbench.engine.clients.grafana.GrafanaClientConfig;
import io.nosqlbench.engine.clients.grafana.transfer.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GrafanaRegionAnalyzer implements Runnable {

    private final Logger logger = LogManager.getLogger(GrafanaRegionAnalyzer.class);

    private String baseUrl = "";
    //    private String dashboardId = "";
    private GrafanaClient gclient;

    public static void main(String[] args) {
        GrafanaRegionAnalyzer analyzer = new GrafanaRegionAnalyzer();
        analyzer.setBaseUrl("http://<host>:<port>/");
//        analyzer.setDashboardId("<<dashboard_id>>");
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
        // params: ActivityDef:(4)/{keycount=5000000000L, hosts=node1, main-cycles=500, threads=1, workload=./keyvalue.yaml, cycles=2, stride=2, tags=block:'schema.*', password=cassandra, rf=3, pooling=16:16:500, driver=cql, rampup-cycles=5000000000, alias=keyvalue_default_schema, valuecount=5000000000L, errors=count, username=cassandra}
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

        List<GDataSource> datasources = getClient().getDatasources();

        List<GPanelDef> mainpanels = db.getPanels().stream()
                .filter(p -> p.getType().equals("graph"))
                .collect(Collectors.toList());

        Map<String, Set<String>> tplValues = getClient().resolveAllTplValues(
                db.getTemplating().getList(),
                db.getTime().getFrom(),
                db.getTime().getTo());
        GStitcher stitcher = new GStitcher(tplValues);


        for (GPanelDef mainpanel : mainpanels) {
            long id = mainpanel.getId();
            String title = mainpanel.getTitle();
            String description = mainpanel.getDescription();

            List<GPanelDef> panels = mainpanel.getPanels();
            String datasource = mainpanel.getDatasource();
            Map<String, Object> fieldConfig = mainpanel.getFieldConfig();
            Map<String, String> options = mainpanel.getOptions();
            List<GPanelDef.GTarget> targets = mainpanel.getTargets();
            logger.info("targets:\n" + targets);

            for (GPanelDef.GTarget target : targets) {
                String expr = target.getExpr();
                expr = stitcher.stitchRegex(expr);
//                expr = GStitcher.resolve(expr,tplValues,GStitcher.Regex);
                logger.info("expr now:" + expr);
                GRangeResult result = getClient().doRangeQuery(mainpanel.getDatasource(), expr, db.getTime().getFrom(), db.getTime().getTo());
//                GQueryResult gqr = getClient().doProxyQuery(mainpanel.getDatasource(), expr, new TypeToken<GQueryResult>() {});
                logger.info(result);
            }

            //logger.info(mainpanel);
        }
        logger.info(mainpanels.size() + " graphs...");

        //http://44.242.139.57:3000/api/datasources/proxy/1/
        // api/v1/query_range?query= result{
        //  type="avg_rate",
        //  avg_of="1m",
        //  alias=~"(keyvalue_default_main|keyvalue_default_rampup|keyvalue_default_schema|keyvalue_main_001
        // |keyvalue_rampup_001|keyvalue_schema_001)"}&start=1607996100&end=1608600900&step=300

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
//            logger.info("creating data snapshot for " + anno);
//            long start = anno.getTime();
//            long end = anno.getTimeEnd();
//            String snapshotKey = "ss-" + dashboardId + "-" + anno.getId();
//            Optional<GSnapshot> snapshot = client.findSnapshotBykeyOptionally(snapshotKey);
//            if (!snapshot.isPresent()) {
//                GSnapshotInfo info = createSnapshotForAnnotation(anno, dashboardMeta.getDashboard(), snapshotKey);
//                GSnapshot newsnapshot = client.findSnapshotBykey(info.getKey());
//            }
//        }
        logger.info("end");
    }

    public GDashboard getDashboard(String dbUid) {
        return getClient().getDashboardByUid(dbUid).getDashboard();
    }
}
