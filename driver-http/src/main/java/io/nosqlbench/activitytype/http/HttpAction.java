package io.nosqlbench.activitytype.http;

import com.codahale.metrics.Timer;
import io.nosqlbench.activitytype.cmds.HttpOp;
import io.nosqlbench.activitytype.cmds.ReadyHttpOp;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.core.templates.StringBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


public class HttpAction implements SyncAction {

    private final static Logger logger = LoggerFactory.getLogger(HttpAction.class);

    private final HttpActivity httpActivity;
    private final int slot;
    private int maxTries = 1;
    private boolean showstmts;

    private OpSequence<ReadyHttpOp> sequencer;
    private HttpClient client;

    private HttpResponse.BodyHandler<String> bodyreader = HttpResponse.BodyHandlers.ofString();
    private long timeoutMillis=30000L;


    public HttpAction(ActivityDef activityDef, int slot, HttpActivity httpActivity) {
        this.slot = slot;
        this.httpActivity = httpActivity;
    }

    @Override
    public void init() {
        this.sequencer = httpActivity.getSequencer();
        this.client = initClient(httpActivity.getClientScope());
    }

    private HttpClient initClient(ClientScope clientScope) {
        switch (clientScope) {
            case thread:
                return httpActivity.newClient();
            case activity:
               return httpActivity.getClient(Thread.currentThread());
            default:
                throw new RuntimeException("unrecognized client scope: " + clientScope);
        }
    }

    @Override
    public int runCycle(long cycleValue) {
        StringBindings stringBindings;
        String statement = null;
        InputStream result = null;

        // The bind timer captures all the time involved in preparing the
        // operation for execution, including data generation as well as
        // op construction

        // The request to be used must be constructed from the template each time.
        HttpOp httpOp=null;

        // A specifier for what makes a response ok. If this is provided, then it is
        // either a list of valid http status codes, or if non-numeric, a regex for the body
        // which must match.
        // If not provided, then status code 200 is the only thing required to be matched.
        String ok;

        try (Timer.Context bindTime = httpActivity.bindTimer.time()) {
            ReadyHttpOp readHTTPOperation = httpActivity.getSequencer().get(cycleValue);
            httpOp =readHTTPOperation.apply(cycleValue);
        } catch (Exception e) {
            throw new RuntimeException("while binding request in cycle " + cycleValue + ": " + e.getMessage(),e);
        } finally {
            if (httpActivity.isDiagnosticMode()) {
                System.out.println("==== cycle " + cycleValue + " DIAGNOSTICS ====");
                if (httpOp!=null) {
                    httpActivity.console.summarizeRequest(httpOp.request,System.out,cycleValue);
                } else {
                    System.out.println("---- REQUEST was null");
                }
            }
        }

        int tries = 0;
        while (tries < maxTries) {
            tries++;

            CompletableFuture<HttpResponse<String>> responseFuture;
            try (Timer.Context executeTime = httpActivity.executeTimer.time()) {
                responseFuture = client.sendAsync(httpOp.request, this.bodyreader);
            } catch (Exception e) {
                throw new RuntimeException("while waiting for response in cycle " + cycleValue + ":" + e.getMessage(), e);
            }

            HttpResponse<String> response=null;
            long startat = System.nanoTime();
            Exception error = null;
            try {
                response = responseFuture.get(httpActivity.getTimeoutMs(), TimeUnit.MILLISECONDS);
                if (httpOp.ok_status!=null) {
                    if (!String.valueOf(response.statusCode()).matches(httpOp.ok_status)) {
                        throw new InvalidStatusCodeException(cycleValue,httpOp.ok_status,response.statusCode());
                    }
                }
                if (httpOp.ok_body!=null) {
                    if (!response.body().matches(httpOp.ok_body)) {
                        throw new InvalidResponseBodyException(cycleValue, httpOp.ok_body, response.body());
                    }
                }
            } catch (Exception e) {
                error = new RuntimeException("while waiting for response in cycle " + cycleValue + ":" + e.getMessage(), e);
            } finally {
                long nanos = System.nanoTime() - startat;
                httpActivity.resultTimer.update(nanos, TimeUnit.NANOSECONDS);
                if (error==null) {
                    httpActivity.resultSuccessTimer.update(nanos, TimeUnit.NANOSECONDS);
                }
                if (httpActivity.isDiagnosticMode()) {
                    if (response!=null) {
                        httpActivity.console.summarizeResponse(response,System.out,cycleValue,nanos);
                    } else {
                        System.out.println("---- RESPONSE was null");
                    }
                    System.out.println();
                }

                if (error!=null) {
                    // count and log exception types
                }
            }


//            if (ok == null) {
//                if (response.statusCode() != 200) {
//                    throw new ResponseError("Result had status code " +
//                            response.statusCode() + ", but 'ok' was not set for this statement," +
//                            "so it is considered an error.");
//                }
//            } else {
//                String[] oks = ok.split(",");
//                for (String ok_condition : oks) {
//                    if (ok_condition.charAt(0)>='0' && ok_condition.charAt(0)<='9') {
//                        int matching_status = Integer.parseInt(ok_condition);
//                    } else {
//                        Pattern successRegex = Pattern.compile(ok);
//                    }
//                }
////                Matcher matcher = successRegex.matcher(String.valueOf(response.statusCode()));
////                if (!matcher.matches()) {
////                    throw new BasicError("status code " + response.statusCode() + " did not match " + success);
////                }
//            }
        }
        return 0;
    }

//                String body = future.body();


//            String[] splitStatement = statement.split("\\?");
//            String path, query;
//
//            path = splitStatement[0];
//            query = "";
//
//            if (splitStatement.length >= 2) {
//                query = splitStatement[1];
//            }
//
//            URI uri = new URI(
//                "http",
//                null,
//                host,
//                httpActivity.getPort(),
//                path,
//                query,
//                null);
//
//                statement = uri.toString();
//
//            showstmts = httpActivity.getShowstmts();

//            if (showstmts) {
//                logger.info("STMT(cycle=" + cycleValue + "):\n" + statement);
//            }
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
//
//        long nanoStartTime=System.nanoTime();
//

//                Timer.Context resultTime = httpActivity.resultTimer.time();
//                try {
//                    StringBuilder res = new StringBuilder();
//
//                    BufferedReader rd = new BufferedReader(new InputStreamReader(result));
//                    String line;
//                    while ((line = rd.readLine()) != null) {
//                        res.append(line);
//                    }
//                    rd.close();
//
//                } catch (Exception e) {
//                    long resultNanos = resultTime.stop();
//                    resultTime = null;
//                } finally {
//                    if (resultTime != null) {
//                        resultTime.stop();
//                    }
//
//                }
//
//            }
//            long resultNanos = System.nanoTime() - nanoStartTime;
//            httpActivity.resultSuccessTimer.update(resultNanos, TimeUnit.NANOSECONDS);


//        protected HttpActivity getHttpActivity () {
//            return httpActivity;
//        }
//    }

    private HttpRequest.BodyPublisher bodySourceFrom(Map<String, String> cmdMap) {
        if (cmdMap.containsKey("body")) {
            String body = cmdMap.remove("body");
            return HttpRequest.BodyPublishers.ofString(body);
        } else if (cmdMap.containsKey("file")) {
            try {
                String file = cmdMap.get("file");
                Path path = Path.of(file);
                return HttpRequest.BodyPublishers.ofFile(path);
            } catch (FileNotFoundException e) {
                throw new BasicError("Could not find file content for request at " + cmdMap.get("file"));
            }
        } else {
            return HttpRequest.BodyPublishers.noBody();
        }

    }


}