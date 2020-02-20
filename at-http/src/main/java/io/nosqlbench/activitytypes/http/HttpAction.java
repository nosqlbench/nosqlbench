package io.nosqlbench.activitytypes.http;

import com.codahale.metrics.Timer;
import io.nosqlbench.activityapi.core.SyncAction;
import io.nosqlbench.activityapi.planning.OpSequence;
import io.nosqlbench.activityimpl.ActivityDef;
import io.virtdata.templates.StringBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;


public class HttpAction implements SyncAction {

    private final static Logger logger = LoggerFactory.getLogger(HttpAction.class);
    private final HttpActivity httpActivity;
    private final int slot;
    private int maxTries = 1;
    private boolean showstmts;

    private OpSequence<StringBindings> sequencer;


    public HttpAction(ActivityDef activityDef, int slot, HttpActivity httpActivity) {
        this.slot = slot;
        this.httpActivity = httpActivity;
    }

    @Override
    public void init() {
        this.sequencer = httpActivity.getOpSequence();
    }

    @Override
    public int runCycle(long cycleValue) {
        StringBindings stringBindings;
        String statement = null;
        InputStream result = null;

        try (Timer.Context bindTime = httpActivity.bindTimer.time()) {
            stringBindings = sequencer.get(cycleValue);
            statement = stringBindings.bind(cycleValue);

            String[] splitStatement = statement.split("\\?");
            String path, query;

            String host = httpActivity.getHosts()[(int) cycleValue % httpActivity.getHosts().length];

            path = splitStatement[0];
            query = "";

            if (splitStatement.length >= 2) {
                query = splitStatement[1];
            }

            URI uri = new URI(
                "http",
                null,
                host,
                httpActivity.getPort(),
                path,
                query,
                null);

                statement = uri.toString();

            showstmts = httpActivity.getShowstmts();
            if (showstmts) {
                logger.info("STMT(cycle=" + cycleValue + "):\n" + statement);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        long nanoStartTime=System.nanoTime();
        int tries = 0;

        while (tries < maxTries) {
            tries++;

            try (Timer.Context executeTime = httpActivity.executeTimer.time()) {
                URL url = new URL(statement);
//
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                result = conn.getInputStream();
            } catch (Exception e) {
                throw new RuntimeException("Error writing output:" + e, e);
            }

            Timer.Context resultTime = httpActivity.resultTimer.time();
            try {
                StringBuilder res = new StringBuilder();

                BufferedReader rd = new BufferedReader(new InputStreamReader(result));
                String line;
                while ((line = rd.readLine()) != null) {
                    res.append(line);
                }
                rd.close();

            } catch (Exception e) {
                long resultNanos = resultTime.stop();
                resultTime=null;
            } finally {
                if (resultTime!=null) {
                    resultTime.stop();
                }

            }

        }
        long resultNanos=System.nanoTime() - nanoStartTime;
        httpActivity.resultSuccessTimer.update(resultNanos, TimeUnit.NANOSECONDS);


        return 0;
    }
    protected HttpActivity getHttpActivity() {
        return httpActivity;
    }
}