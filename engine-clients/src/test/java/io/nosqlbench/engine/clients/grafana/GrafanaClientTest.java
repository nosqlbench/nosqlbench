package io.nosqlbench.engine.clients.grafana;

import io.nosqlbench.engine.clients.grafana.transfer.Annotation;
import io.nosqlbench.engine.clients.grafana.transfer.Annotations;
import org.junit.Ignore;
import org.junit.Test;

public class GrafanaClientTest {
    private static final String testurl = "http://localhost:3000/";

    @Test
    @Ignore
    public void testCreateAnnotation() {
        GrafanaClient client = new GrafanaClient(testurl);
        client.basicAuth("admin", "admin");
        Annotation a = new Annotation();
        a.setDashboardId(2);
        a.setText("testingAnnotation");
        Annotation created = client.createAnnotation(a);
        System.out.println(created);
    }

    @Test
    @Ignore
    public void testFindAnnotations() {
        GrafanaClient client = new GrafanaClient(testurl);
        client.basicAuth("admin", "admin");
        Annotations annotations = client.findAnnotations(By.id(1));
        System.out.println(annotations);

    }
}
