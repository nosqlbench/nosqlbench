package io.nosqlbench.engine.docker;

import io.nosqlbench.engine.api.exceptions.BasicError;
import io.nosqlbench.nb.api.pathutil.NBFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class RestHelper {
    private static Logger logger = LoggerFactory.getLogger(RestHelper.class);

    static HttpClient.Builder clientBuilder = HttpClient.newBuilder();
    static HttpClient httpClient = clientBuilder.build();


    private static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }


    public static HttpResponse<String> post(String url, String path, boolean auth, String taskname) {
        logger.debug("posting to " + url + " with path:" + path +", auth: " + auth + " task:" + taskname);

        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder = builder.uri(URI.create(url));
        if (auth) {
            // do not, DO NOT put authentication here that is not a well-known default already
            // DO prompt the user to configure a new password on first authentication
            builder = builder.header("Authorization", basicAuth("admin", "admin"));
        }

        if (path !=null) {
            logger.debug("POSTing " + path + " to " + url);
            String dashboard = NBFiles.readFile(path);
            logger.debug("length of content for " + path + " is " + dashboard.length());
            builder = builder.POST(HttpRequest.BodyPublishers.ofString(dashboard));
            builder.setHeader("Content-Type", "application/json");
        } else {
            logger.debug(("POSTing empty body to " + url));
            builder = builder.POST(HttpRequest.BodyPublishers.noBody());
        }

        HttpRequest request = builder.build();

        try {
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.debug("http response for configuring grafana:\n" + resp);
            logger.debug("response status code: " + resp.statusCode());
            logger.debug("response body: " + resp.body());
            if (resp.statusCode()==412) {
                logger.warn("Unable to configure dashboard, grafana precondition failed (status 412): " + resp.body());
                String err = "When trying to configure grafana, any errors indicate that you may be trying to RE-configure an instance." +
                    " This may be a bug. If you already have a docker stack running, you can just use '--report-graphite-to localhost:9109'\n" +
                    " instead of --docker-metrics.";
                throw new BasicError(err);
            } else if (resp.statusCode()==401 && resp.body().contains("Invalid username")) {
                logger.warn("Unable to configure dashboard, grafana authentication failed (status " + resp.statusCode() + "): " + resp.body());
                String err = "Grafana does not have the same password as expected for a new container. We shouldn't be trying to add dashboards on an" +
                    " existing container. This may be a bug. If you already have a docker stack running, you can just use '--report-graphite-to localhost:9109'" +
                    " instead of --docker-metrics.";
                throw new BasicError(err);
            } else if (resp.statusCode()<200 || resp.statusCode()>200) {
                logger.error("while trying to " + taskname +", received status code " + resp.statusCode() + " while trying to auto-configure grafana, with body:");
                logger.error(resp.body());
                throw new RuntimeException("while trying to " + taskname + ", received status code " + resp.statusCode() + " response for " + url + " with body: " + resp.body());
            }
            return resp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
