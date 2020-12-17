package io.nosqlbench.engine.clients.grafana.analyzer;

import io.nosqlbench.engine.clients.grafana.transfer.GDashboard;
import org.junit.Test;

public class GrafanaRegionAnalyzerTest {

    @Test
    public void testGetQueries() {
        GrafanaRegionAnalyzer gra = new GrafanaRegionAnalyzer();
        gra.setBaseUrl("http://44.242.139.57:3000/");
        GDashboard db = GDashboard.fromFile("examples/db.json");

        gra.getQueries(db);

    }

}