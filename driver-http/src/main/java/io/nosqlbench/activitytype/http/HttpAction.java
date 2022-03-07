package io.nosqlbench.activitytype.http;

import com.codahale.metrics.Timer;
import io.nosqlbench.activitytype.cmds.HttpOp;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;


public class HttpAction implements SyncAction {

    private final static Logger logger = LogManager.getLogger(HttpAction.class);

    private final HttpActivity httpActivity;
    private final int slot;
    private int maxTries = 1;

    private OpSequence<OpDispenser<? extends HttpOp>> sequencer;
    private HttpClient client;

    private final HttpResponse.BodyHandler<String> bodyreader = HttpResponse.BodyHandlers.ofString();

    public HttpAction(ActivityDef activityDef, int slot, HttpActivity httpActivity) {
        this.slot = slot;
        this.httpActivity = httpActivity;
    }

    @Override
    public void init() {
        this.sequencer = httpActivity.getSequencer();
        this.client = initClient(httpActivity.getClientScope());
        this.maxTries = httpActivity.getActivityDef().getParams().getOptionalInteger("maxtries").orElse(10);
    }

    private HttpClient initClient(ClientScope clientScope) {
        return httpActivity.getClient().apply(Thread.currentThread());
    }

    @Override
    public int runCycle(long cycle) {
        int tries = 0;

        // The request to be used must be constructed from the template each time.
        HttpOp httpOp = null;

        // The bind timer captures all the time involved in preparing the
        // operation for execution, including data generation as well as
        // op construction
        try (Timer.Context bindTime = httpActivity.bindTimer.time()) {
            LongFunction<? extends HttpOp> readyOp = sequencer.apply(cycle);
            httpOp = readyOp.apply(cycle);
        } catch (Exception e) {
            if (httpActivity.isDiagnosticMode()) {
                if (httpOp != null) {
                    httpActivity.console.summarizeRequest("ERRORED REQUEST", e, httpOp.request, System.out, cycle,
                        System.nanoTime());
                } else {
                    System.out.println("---- REQUEST was null");
                }
            }
            throw new RuntimeException("while binding request in cycle " + cycle + ": " + e.getMessage(), e);
        } finally {
        }

        int resultCode=0;

        while (tries < maxTries) {
            tries++;

            CompletableFuture<HttpResponse<String>> responseFuture;
            try (Timer.Context executeTime = httpActivity.executeTimer.time()) {
                responseFuture = client.sendAsync(httpOp.request, this.bodyreader);
            } catch (Exception e) {
                throw new RuntimeException("while waiting for response in cycle " + cycle + ":" + e.getMessage(), e);
            }

            HttpResponse<String> response = null;
            long startat = System.nanoTime();
            Exception error = null;
            try {
                response = responseFuture.get(httpActivity.getTimeoutMillis(), TimeUnit.MILLISECONDS);
                httpActivity.statusCodeHisto.update(response.statusCode());
                if (httpOp.ok_status != null) {
                    if (!httpOp.ok_status.matcher(String.valueOf(response.statusCode())).matches()) {
                        throw new InvalidStatusCodeException(cycle, httpOp.ok_status, response.statusCode());
                    }
                }
                if (httpOp.ok_body != null) {
                    if (!httpOp.ok_body.matcher(response.body()).matches()) {
                        throw new InvalidResponseBodyException(cycle, httpOp.ok_body, response.body());
                    }
                }
            } catch (Exception e) {
                error = e;
//                error = new RuntimeException("while waiting for response in cycle " + cycleValue + ":" + e.getMessage(), e);
            } finally {
                long nanos = System.nanoTime() - startat;
                httpActivity.resultTimer.update(nanos, TimeUnit.NANOSECONDS);
                if (error == null) {
                    httpActivity.resultSuccessTimer.update(nanos, TimeUnit.NANOSECONDS);
                }
                if (httpActivity.isDiagnosticMode()) {
                    httpActivity.console.summarizeRequest("request", error, httpOp.request, System.out, cycle, nanos);
                    if (response != null) {
                        httpActivity.console.summarizeResponseChain(error, response, System.out, cycle, nanos);
                    } else {
                        System.out.println("---- RESPONSE was null");
                    }
                    System.out.println();
                }

                // TODO: use this as a documented example for how to add error handling to a new activity
                if (error == null) {
                    break; // break out of the tries loop without retrying, because there was no error
                } else {
                    // count and log exception types
                    ErrorDetail detail = httpActivity.getErrorHandler().handleError(error, cycle, nanos);
                    resultCode=detail.resultCode;
                    if (!detail.isRetryable()) {
                        break; // break out of the tries loop without retrying, because the error handler said so
                    }

                }
            }

        }

        httpActivity.triesHisto.update(tries);

        return resultCode;
    }

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
