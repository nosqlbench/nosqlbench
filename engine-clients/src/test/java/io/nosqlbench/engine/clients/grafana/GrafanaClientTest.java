package io.nosqlbench.engine.clients.grafana;

import io.nosqlbench.engine.clients.grafana.transfer.GrafanaAnnotation;
import io.nosqlbench.engine.clients.grafana.transfer.Annotations;
import org.junit.Ignore;
import org.junit.Test;

public class GrafanaClientTest {
    private static final String testurl = "http://localhost:3000/";

    @Test
    @Ignore
    public void testCreateAnnotation() {
        GrafanaClient client = new GrafanaClient(testurl);
        client.getConfig().basicAuth("admin", "admin");
        GrafanaAnnotation a = new GrafanaAnnotation();
        a.setDashboardId(2);
        a.setText("testingAnnotation");
        GrafanaAnnotation created = client.createAnnotation(a);
        System.out.println(created);
    }

    @Test
    @Ignore
    public void testFindAnnotations() {
        GrafanaClient client = new GrafanaClient(testurl);
        client.getConfig().basicAuth("admin", "admin");
        Annotations annotations = client.findAnnotations(By.id(1));
        System.out.println(annotations);
    }

    @Test
    @Ignore
    public void testGetApiToken() {
        GrafanaClient client = new GrafanaClient(testurl);
        client.getConfig().basicAuth("admin", "admin");
        ApiToken token = client.createApiToken("nosqlbench", "Admin", Long.MAX_VALUE);
        System.out.println(token);
    }
}
