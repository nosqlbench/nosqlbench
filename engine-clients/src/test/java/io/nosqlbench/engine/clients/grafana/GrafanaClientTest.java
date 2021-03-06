package io.nosqlbench.engine.clients.grafana;

import io.nosqlbench.engine.clients.grafana.transfer.GAnnotation;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

public class GrafanaClientTest {
    private static final String testurl = "http://localhost:3000/";

    @Test
    @Disabled
    public void testCreateAnnotation() {
        GrafanaClient client = new GrafanaClient(testurl);
        client.getConfig().basicAuth("admin", "admin");
        GAnnotation a = new GAnnotation();
        a.setDashboardId(2);
        a.setText("testingAnnotation");
        GAnnotation created = client.createAnnotation(a);
        System.out.println(created);
    }

    @Test
    @Disabled
    public void testFindAnnotations() {
        GrafanaClient client = new GrafanaClient(testurl);
        client.getConfig().basicAuth("admin", "admin");
        List<GAnnotation> annotations = client.findAnnotations(By.id(1));
        System.out.println(annotations);
    }

    @Test
    @Disabled
    public void testGetApiToken() {
        GrafanaClient client = new GrafanaClient(testurl);
        client.getConfig().basicAuth("admin", "admin");
        ApiToken token = client.createApiToken("nosqlbench", "Admin", Long.MAX_VALUE);
        System.out.println(token);
    }
}
