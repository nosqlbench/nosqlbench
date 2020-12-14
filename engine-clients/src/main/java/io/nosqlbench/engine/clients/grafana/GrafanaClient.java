package io.nosqlbench.engine.clients.grafana;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.engine.clients.grafana.transfer.*;

import java.io.File;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.function.Supplier;

/**
 * @see <a href="https://grafana.com/docs/grafana/latest/http_api/annotations/">Grafana Annotations API Docs</a>
 */
public class GrafanaClient {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final GrafanaClientConfig config;

    public GrafanaClient(GrafanaClientConfig config) {
        this.config = config;
    }

    public GrafanaClient(String baseuri) {
        this(new GrafanaClientConfig().setBaseUri(baseuri));
    }

    public GrafanaClientConfig getConfig() {
        return config;
    }

    /**
     * <pre>{@code
     * GET /api/annotations?from=1506676478816&to=1507281278816&tags=tag1&tags=tag2&limit=100
     *
     * Example Request:
     *
     * GET /api/annotations?from=1506676478816&to=1507281278816&tags=tag1&tags=tag2&limit=100 HTTP/1.1
     * Accept: application/json
     * Content-Type: application/json
     * Authorization: Basic YWRtaW46YWRtaW4=
     * Query Parameters:
     *
     * from: epoch datetime in milliseconds. Optional.
     * to: epoch datetime in milliseconds. Optional.
     * limit: number. Optional - default is 100. Max limit for results returned.
     * alertId: number. Optional. Find annotations for a specified alert.
     * dashboardId: number. Optional. Find annotations that are scoped to a specific dashboard
     * panelId: number. Optional. Find annotations that are scoped to a specific panel
     * userId: number. Optional. Find annotations created by a specific user
     * type: string. Optional. alert|annotation Return alerts or user created annotations
     * tags: string. Optional. Use this to filter global annotations. Global annotations are annotations from an annotation data source that are not connected specifically to a dashboard or panel. To do an “AND” filtering with multiple tags, specify the tags parameter multiple times e.g. tags=tag1&tags=tag2.
     * Example Response:
     *
     * HTTP/1.1 200
     * Content-Type: application/json
     * [
     *     {
     *         "id": 1124,
     *         "alertId": 0,
     *         "dashboardId": 468,
     *         "panelId": 2,
     *         "userId": 1,
     *         "userName": "",
     *         "newState": "",
     *         "prevState": "",
     *         "time": 1507266395000,
     *         "timeEnd": 1507266395000,
     *         "text": "test",
     *         "metric": "",
     *         "type": "event",
     *         "tags": [
     *             "tag1",
     *             "tag2"
     *         ],
     *         "data": {}
     *     },
     *     {
     *         "id": 1123,
     *         "alertId": 0,
     *         "dashboardId": 468,
     *         "panelId": 2,
     *         "userId": 1,
     *         "userName": "",
     *         "newState": "",
     *         "prevState": "",
     *         "time": 1507265111000,
     *         "text": "test",
     *         "metric": "",
     *         "type": "event",
     *         "tags": [
     *             "tag1",
     *             "tag2"
     *         ],
     *         "data": {}
     *     }
     * ]
     * }</pre>
     *
     * @param by
     * @return
     */
    public Annotations findAnnotations(By... by) {

        String query = By.fields(by);
        HttpRequest.Builder rqb = config.newRequest("api/annotations?" + query);
        rqb.setHeader("Content-Type", "application/json");
        HttpRequest request = rqb.build();

        HttpClient client = config.newClient();
        HttpResponse<String> response = null;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String body = response.body();
        Annotations annotations = gson.fromJson(body, Annotations.class);
        return annotations;
    }

    /**
     * <pre>{@code
     * POST /api/annotations
     *
     * Example Request:
     *
     * POST /api/annotations HTTP/1.1
     * Accept: application/json
     * Content-Type: application/json
     *
     * {
     *   "dashboardId":468,
     *   "panelId":1,
     *   "time":1507037197339,
     *   "timeEnd":1507180805056,
     *   "tags":["tag1","tag2"],
     *   "text":"Annotation Description"
     * }
     * Example Response:
     *
     * HTTP/1.1 200
     * Content-Type: application/json
     *
     * {
     *     "message":"Annotation added",
     *     "id": 1,
     * }
     * }</pre>
     *
     * @return
     */
    public GrafanaAnnotation createAnnotation(GrafanaAnnotation grafanaAnnotation) {
        HttpClient client = config.newClient();
        HttpRequest.Builder rqb = config.newRequest("api/annotations");
        rqb.setHeader("Content-Type", "application/json");
        String rqBody = gson.toJson(grafanaAnnotation);
        rqb = rqb.POST(HttpRequest.BodyPublishers.ofString(rqBody));

        HttpResponse<String> response = null;
        try {
            response = client.send(rqb.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            if (e.getMessage().contains("WWW-Authenticate header missing")) {
                throw new RuntimeException("Java HttpClient was not authorized, and it saw no WWW-Authenticate header" +
                        " in the response, so this is probably Grafana telling you that the auth scheme failed. Normally " +
                        "this error would be thrown by Java HttpClient:" + e.getMessage());
            }
            throw new RuntimeException(e);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Creating annotation failed with status code " + response.statusCode() + " at " +
                    "baseuri " + config.getBaseUri() + ": " + response.body());
        }
        String body = response.body();
        GrafanaAnnotation savedGrafanaAnnotation = gson.fromJson(body, GrafanaAnnotation.class);
        return savedGrafanaAnnotation;
    }


    public DashboardResponse getDashboardByUid(String uid) {
        HttpClient client = config.newClient();
        HttpRequest.Builder rqb = config.newRequest("api/dashboards/uid/" + uid);
        rqb = rqb.GET();

        HttpResponse<String> response = null;
        try {
            response = client.send(rqb.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Getting dashboard by uid (" + uid + ") failed with status code " + response.statusCode() +
                    " at baseuri " + config.getBaseUri() + ": " + response.body());
        }
        String body = response.body();

        DashboardResponse dashboardResponse = gson.fromJson(body, DashboardResponse.class);
        return dashboardResponse;
    }

    public DashboardSnapshot createSnapshot() {

    }

    /**
     * <pre>{@code
     * POST /api/annotations/graphite
     *
     * Example Request:
     *
     * POST /api/annotations/graphite HTTP/1.1
     * Accept: application/json
     * Content-Type: application/json
     *
     * {
     *   "what": "Event - deploy",
     *   "tags": ["deploy", "production"],
     *   "when": 1467844481,
     *   "data": "deploy of master branch happened at Wed Jul 6 22:34:41 UTC 2016"
     * }
     * Example Response:
     *
     * HTTP/1.1 200
     * Content-Type: application/json
     *
     * {
     *     "message":"Graphite annotation added",
     *     "id": 1
     * }
     * }</pre>
     *
     * @return
     */
    public GrafanaAnnotation createGraphiteAnnotation() {
        throw new RuntimeException("unimplemented");
    }

    /**
     * <pre>{@code
     * PUT /api/annotations/:id
     *
     * Updates all properties of an annotation that matches the specified id. To only update certain property, consider using the Patch Annotation operation.
     *
     * Example Request:
     *
     * PUT /api/annotations/1141 HTTP/1.1
     * Accept: application/json
     * Authorization: Bearer eyJrIjoiT0tTcG1pUlY2RnVKZTFVaDFsNFZXdE9ZWmNrMkZYbk
     * Content-Type: application/json
     *
     * {
     *   "time":1507037197339,
     *   "timeEnd":1507180805056,
     *   "text":"Annotation Description",
     *   "tags":["tag3","tag4","tag5"]
     * }
     * Example Response:
     *
     * HTTP/1.1 200
     * Content-Type: application/json
     *
     * {
     *     "message":"Annotation updated"
     * }
     * }</pre>
     */
    public void updateAnnotation() {
        throw new RuntimeException("unimplemented");
    }

    /**
     * <pre>{@code
     * PATCH /api/annotations/:id
     *
     * Updates one or more properties of an annotation that matches the specified id.
     *
     * This operation currently supports updating of the text, tags, time and timeEnd properties.
     *
     * Example Request:
     *
     * PATCH /api/annotations/1145 HTTP/1.1
     * Accept: application/json
     * Authorization: Bearer eyJrIjoiT0tTcG1pUlY2RnVKZTFVaDFsNFZXdE9ZWmNrMkZYbk
     * Content-Type: application/json
     *
     * {
     *   "text":"New Annotation Description",
     *   "tags":["tag6","tag7","tag8"]
     * }
     * Example Response:
     *
     * HTTP/1.1 200
     * Content-Type: application/json
     *
     * {
     *     "message":"Annotation patched"
     * }
     * }</pre>
     */
    public void patchAnnotation() {
        throw new RuntimeException("unimplemented");
    }

    /**
     * <pre>{@code
     * Example Request:
     *
     * DELETE /api/annotations/1 HTTP/1.1
     * Accept: application/json
     * Content-Type: application/json
     * Authorization: Bearer eyJrIjoiT0tTcG1pUlY2RnVKZTFVaDFsNFZXdE9ZWmNrMkZYbk
     * Example Response:
     *
     * HTTP/1.1 200
     * Content-Type: application/json
     *
     * {
     *     "message":"Annotation deleted"
     * }
     * }</pre>
     *
     * @param id
     */
    public void deleteAnnotation(long id) {
        throw new RuntimeException("unimplemented");
    }

    /**
     * This can be called to create an api token and store it for later use as long as you
     * have the admin credentials for basic auth. This is preferred to continuing to
     * passing basic auth for admin privileges. The permissions can now be narrowed or managed
     * in a modular way.
     *
     * @param namer       the principal name for the privelege
     * @param role        the Grafana role
     * @param ttl         Length of validity for the granted api token
     * @param keyfilePath The path of the token. If it is present it will simply be used.
     * @param un          The basic auth username for the Admin role
     * @param pw          The basic auth password for the Admin role
     */
    public void cacheApiToken(Supplier<String> namer, String role, long ttl, Path keyfilePath, String un, String pw) {
        if (!Files.exists(keyfilePath)) {
            GrafanaClientConfig basicClientConfig = config.copy();
            basicClientConfig = basicClientConfig.basicAuth(un, pw);
            GrafanaClient apiClient = new GrafanaClient(basicClientConfig);
            String keyName = namer.get();
            ApiToken apiToken = apiClient.createApiToken(keyName, role, ttl);
            try {
                if (keyfilePath.toString().contains(File.separator)) {
                    Files.createDirectories(keyfilePath.getParent(),
                            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwx---")));
                }
                Files.writeString(keyfilePath, apiToken.getKey());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        GrafanaMetricsAnnotator.AuthWrapper authHeaderSupplier = new GrafanaMetricsAnnotator.AuthWrapper(
                "Authorization",
                new GrafanaKeyFileReader(keyfilePath),
                s -> "Bearer " + s
        );
        config.addHeaderSource(authHeaderSupplier);
    }

    public ApiToken createApiToken(String name, String role, long ttl) {
        ApiTokenRequest r = new ApiTokenRequest(name, role, ttl);
        ApiToken token = postApiTokenRequest(r, ApiToken.class, "gen api token");
        return token;
    }

    private <T> T postApiTokenRequest(Object request, Class<? extends T> clazz, String desc) {
        HttpRequest rq = config.newJsonPOST("api/auth/keys", request);
        HttpClient client = config.newClient();

        HttpResponse<String> response = null;
        try {
            response = client.send(rq, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            if (e.getMessage().contains("WWW-Authenticate header missing")) {
                throw new RuntimeException("Java HttpClient was not authorized, and it saw no WWW-Authenticate header" +
                        " in the response, so this is probably Grafana telling you that the auth scheme failed. Normally " +
                        "this error would be thrown by Java HttpClient:" + e.getMessage());
            }
            throw new RuntimeException(e);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Request to grafana failed with status code " + response.statusCode() + "\n" +
                    " while trying to '" + desc + "'\n at baseuri " + config.getBaseUri() + ": " + response.body());
        }
        String body = response.body();
        T result = gson.fromJson(body, clazz);
        return result;
    }

}
