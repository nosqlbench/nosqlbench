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

package io.nosqlbench.engine.docker;

import io.nosqlbench.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.function.Supplier;

public class RestHelper {
    private static final Logger logger = LogManager.getLogger(RestHelper.class);

    static HttpClient.Builder clientBuilder = HttpClient.newBuilder();
    static HttpClient httpClient = clientBuilder.build();


    private static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }


    public static HttpResponse<String> post(String url,
                                            Supplier<String> contentSupplier, boolean auth,
                                            String taskname) {
        String content =null;
        if (contentSupplier!=null) {
            content = contentSupplier.get();
        }

        logger.debug("posting to " + url + ", auth: " + auth + " task:" + taskname);

        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder = builder.uri(URI.create(url));
        if (auth) {
            // do not, DO NOT put authentication here that is not a well-known default already
            // DO prompt the user to configure a new password on first authentication
            builder = builder.header("Authorization", basicAuth("admin", "admin"));
        }

        if (content !=null) {
            logger.debug("POSTing " + content.length() + "bytes to " + url + " for " + taskname);
            builder = builder.POST(HttpRequest.BodyPublishers.ofString(content));
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
