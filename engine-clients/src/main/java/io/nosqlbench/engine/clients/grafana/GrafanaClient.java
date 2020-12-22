package io.nosqlbench.engine.clients.grafana;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.nosqlbench.engine.clients.grafana.annotator.GrafanaMetricsAnnotator;
import io.nosqlbench.engine.clients.grafana.transfer.*;
import io.nosqlbench.engine.clients.prometheus.*;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @see <a href="https://grafana.com/docs/grafana/latest/http_api/annotations/">Grafana Annotations API Docs</a>
 */
public class GrafanaClient {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final GrafanaClientConfig config;
    private List<GDataSource> datasources;

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
    public List<GAnnotation> findAnnotations(By... by) {

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
        Type gtype = new TypeToken<List<GAnnotation>>() {
        }.getType();

        List<GAnnotation> annotations = gson.fromJson(body, gtype);
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
    public GAnnotation createAnnotation(GAnnotation gAnnotation) {
        HttpClient client = config.newClient();
        HttpRequest.Builder rqb = config.newRequest("api/annotations");
        rqb.setHeader("Content-Type", "application/json");
        String rqBody = gson.toJson(gAnnotation);
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
        GAnnotation savedGAnnotation = gson.fromJson(body, GAnnotation.class);
        return savedGAnnotation;
    }

    public List<GDashboardInfo> findDashboards() {
        HttpClient client = config.newClient();
        HttpRequest.Builder rqb = config.newRequest("api/search?type=dash-db");
        rqb = rqb.GET();

        HttpResponse<String> response = null;
        try {
            response = client.send(rqb.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Getting list of dashboards failed with status code " + response.statusCode() +
                    " at baseuri " + config.getBaseUri() + ": " + response.body());
        }
        String body = response.body();
        Type dblist = new TypeToken<List<GDashboardInfo>>() {
        }.getType();
        List<GDashboardInfo> results = gson.fromJson(body, dblist);
        return results;

    }

    public GSnapshot findSnapshotBykey(String snapshotKey) {
        HttpClient client = config.newClient();
        HttpRequest.Builder rqb = config.newRequest("api/snapshots/" + snapshotKey);
        rqb = rqb.GET();

        HttpResponse<String> response = null;
        try {
            response = client.send(rqb.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Getting dashboard snapshot for key '" + snapshotKey + "' failed with status " +
                    "code " + response.statusCode() +
                    " at baseuri " + config.getBaseUri() + ": " + response.body());
        }

        String body = response.body();

        GSnapshot snapshot = gson.fromJson(body, GSnapshot.class);
        return snapshot;

    }

    public Optional<GSnapshot> findSnapshotBykeyOptionally(String snapshotKey) {
        try {
            GSnapshot snapshotBykey = findSnapshotBykey(snapshotKey);
            return Optional.of(snapshotBykey);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<GSnapshotInfo> findSnapshots() {
        HttpClient client = config.newClient();
        HttpRequest.Builder rqb = config.newRequest("api/dashboard/snapshots");
        rqb = rqb.GET();

        HttpResponse<String> response = null;
        try {
            response = client.send(rqb.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Getting dashboard snapshots failed with status code " + response.statusCode() +
                    " at baseuri " + config.getBaseUri() + ": " + response.body());
        }
        String body = response.body();

        Type t = new TypeToken<List<GSnapshotInfo>>() {
        }.getType();
        List<GSnapshotInfo> snapshotsInfo = gson.fromJson(body, t);
        return snapshotsInfo;

    }


    public GDashboardResponse getDashboardByUid(String uid) {
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

        GDashboardResponse dashboardMeta = gson.fromJson(body, GDashboardResponse.class);
        return dashboardMeta;
    }

    public GSnapshotInfo createSnapshot(GDashboard dashboard, String snid) {

        HttpClient client = config.newClient();
        HttpRequest.Builder rqb = config.newRequest("api/snapshots");
        rqb = rqb.setHeader("Content-Type", "application/json");
        String rqBody = gson.toJson(new CreateSnapshotRequest(dashboard, null, snid));
        rqb = rqb.POST(HttpRequest.BodyPublishers.ofString(rqBody));

        HttpResponse<String> response = null;
        try {
            response = client.send(rqb.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Creating snapshot for snid (" + snid + ") failed with status code " + response.statusCode() +
                    " at baseuri " + config.getBaseUri() + ": " + response.body());
        }
        String body = response.body();

        GSnapshotInfo snapshotInfo = gson.fromJson(body, GSnapshotInfo.class);

        return snapshotInfo;
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
    public GAnnotation createGraphiteAnnotation() {
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

    public <T> T doProxyQuery(String dsname, String path, String query, TypeToken<? extends T> asType) {
        GDataSource datasource = getCachedDatasource(dsname);
        long dsid = datasource.getId();
        String composedQuery = path;
        if (query != null && !query.isBlank()) {
            composedQuery = composedQuery + "?" + query;
        }

        HttpClient client = config.newClient();
        HttpRequest.Builder rqb =
                config.newRequest("api/datasources/proxy/" + dsid + "/" + composedQuery);
        rqb.setHeader("Accept", "application/json");
        rqb = rqb.GET();

        HttpResponse<String> response = null;
        try {
            response = client.send(rqb.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Executing proxy query failed with status code " + response.statusCode() +
                    " at baseuri " + config.getBaseUri() + ": " + response.body() + " for datasource '" + dsid + "' and query '" + query + "'");
        }
        String body = response.body();
        T result = gson.fromJson(body, asType.getType());
        return result;
    }

    private GDataSource getCachedDatasource(String dsname) {
        return getCachedDatasources().stream()
                .filter(gd -> gd.getName().equals(dsname))
                .findFirst()
                .orElseThrow();
    }

    public Map<String, Set<String>> resolveAllTplValues(List<GTemplate> tpls, String timeStart, String timeEnd) {
        Map<String, Set<String>> allTplValues = new HashMap<>();
        for (GTemplate gTemplate : tpls) {
            Set<String> strings = resolveTplValues(gTemplate, timeStart, timeEnd, getCachedDatasources());
            allTplValues.put(gTemplate.getName(), strings);
        }
        return allTplValues;
    }


    public Set<String> resolveTplValues(GTemplate tpl, String timeStart, String timeEnd, List<GDataSource> dss) {
        Set<String> resolved = new HashSet<>();

        List<String> values = tpl.getCurrent().getValues();

        if (values.size() == 1 && values.get(0).equals("$__all")) {
            if (tpl.getAllValue() != null && !tpl.getAllValue().isBlank()) {
                resolved.add(tpl.getAllValue());
            } else {


                String dsname = tpl.getDatasource();
                Optional<GDataSource> dso = dss.stream().filter(n -> n.getName().equals(dsname)).findFirst();
                GDataSource ds = dso.orElseThrow();

                String query = tpl.getQuery();
                String formatted = formatSeriesQuery(ds.getType(), query, timeStart, timeEnd);

                if (ds.getType().equals("prometheus")) {
                    long startSpec = GTimeUnit.epochSecondsFor(timeStart);
                    long endSpec = GTimeUnit.epochSecondsFor(timeEnd);
                    String q = "api/v1/series?match[]=" + URLEncoder.encode(tpl.getQuery()) +
                            "&start=" + startSpec +
                            "&end=" + endSpec;
                    PromSeriesLookupResult psr = doProxyQuery("prometheus", "api/v1/series", q, new TypeToken<PromSeriesLookupResult>() {
                    });
                    for (PromSeriesLookupResult.Element elem : psr.getData()) {
                        String elementSpec = elem.toString();
                        String regex = tpl.getRegex();
                        if (regex != null && !regex.isBlank()) {
                            if (regex.startsWith("/")) {
                                regex = regex.substring(1, regex.length() - 2);
                            }
                            Pattern p = Pattern.compile(regex);
                            Matcher m = p.matcher(elementSpec);
                            if (m.find()) {
                                String group = m.group(1);
                                resolved.add(group);
                            }
                        } else {
                            resolved.add(elem.toString());
                        }
                    }

                } else {
                    throw new RuntimeException("datasource type not supported yet for template values: '" + ds.getType() + "'");
                }
            }
        } else {
            for (String value : tpl.getCurrent().getValues()) {
                resolved.add(value);
            }
        }

        return resolved;
    }

    private String formatSeriesQuery(String type, String query, String startTime, String endTime) {
        if (type.equals("prometheus")) {
            long startSpec = GTimeUnit.epochSecondsFor(startTime);
            long endSpec = GTimeUnit.epochSecondsFor(endTime);
            return "api/v1/series?match[]=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&start=" + startSpec +
                    "&end=" + endSpec;
        } else {
            throw new RuntimeException("Unknown query target type '" + type + "'");
        }
    }

    public List<GDataSource> getDatasources() {
        HttpClient client = config.newClient();
        HttpRequest.Builder rqb = config.newRequest("api/datasources");
        rqb = rqb.GET();

        HttpResponse<String> response = null;
        try {
            response = client.send(rqb.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Getting datasources failed with status code " + response.statusCode() +
                    " at baseuri " + config.getBaseUri() + ": " + response.body());
        }
        String body = response.body();

        Type dsListType = new TypeToken<List<GDataSource>>() {
        }.getType();
        List<GDataSource> dataSourcesList = gson.fromJson(body, dsListType);
        return dataSourcesList;
    }

    private List<GDataSource> getCachedDatasources() {
        if (datasources == null) {
            datasources = getDatasources();
        }
        return datasources;
    }

    public GRangeResult doRangeQuery(String datasource, String expr, String startSpec, String endSpec) {
        GDataSource ds = getCachedDatasource(datasource);
        if (ds.getType().equals("prometheus")) {
            long start = GTimeUnit.epochSecondsFor(startSpec);
            long end = GTimeUnit.epochSecondsFor(endSpec);
            // http://44.242.139.57:3000/api/datasources/proxy/1/api/v1/query_range?query=result%7Btype%3D%22avg_rate%22%2Cavg_of%3D%221m%22%2Calias%3D~%22keyvalue_main_001%22%7D&start=1608534000&end=1608620400&step=300
            // http://44.242.139.57:3000/api/datasources/proxy/1/api/v1/query_range?query=result%7Btype%3D%22avg_rate%22%2Cavg_of%3D%221m%22%2Calias%3D%7E%22%28.*%29%22%7D&start=1608611971&end=1608622771&step=300
            String path = "api/v1/query_range?query=" +
                    URLEncoder.encode(expr) + "&start=" + start + "&end=" + end + "&step=300";

            PromQueryResult<PMatrixData> vectorData = doProxyQuery(
                    datasource,
                    "api/v1/query_range",
                    "query=" + URLEncoder.encode(expr) + "&start=" + start + "&end=" + end + "&step=300",
                    new TypeToken<PromQueryResult<PMatrixData>>() {
                    });
            System.out.println(vectorData);
            return null;
        } else {
            throw new RuntimeException("data source " + datasource + " is not yet supported.");
        }


        // TODO: Distinguish between datasources named "prometheus" and  data source types "prometheus"
        // TODO: Figure out how to set step equivalently
    }
}
