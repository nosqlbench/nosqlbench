/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.activitytype.stdout;

import io.nosqlbench.engine.api.activityconfig.ParsedStmt;
import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtDef;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityapi.planning.SequencePlanner;
import io.nosqlbench.engine.api.activityapi.planning.SequencerType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.ParameterMap;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.engine.api.metrics.ExceptionMeterMetrics;
import io.nosqlbench.engine.api.util.StrInterpolator;
import io.nosqlbench.virtdata.api.BindingsTemplate;
import io.nosqlbench.virtdata.api.templates.StringBindings;
import io.nosqlbench.virtdata.api.templates.StringBindingsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

@SuppressWarnings("Duplicates")
public class StdoutActivity extends SimpleActivity implements ActivityDefObserver {
    private final static Logger logger = LoggerFactory.getLogger(StdoutActivity.class);
    private final Boolean showstmts;
    private final StmtsDocList stmtsDocList;
    public Timer bindTimer;
    public Timer executeTimer;
    public Timer resultTimer;
    public Histogram triesHisto;
    private Writer pw;
    private String fileName;
    private ExceptionMeterMetrics exceptionMeterMetrics;
    private int retry_delay = 0;
    private int retries;

    public OpSequence<StringBindings> getOpSequence() {
        return opSequence;
    }

    private OpSequence<StringBindings> opSequence;

    public StdoutActivity(ActivityDef activityDef) {
        super(activityDef);
        StrInterpolator interp = new StrInterpolator(activityDef);
        String yaml_loc = activityDef.getParams().getOptionalString("yaml", "workload").orElse("default");

        this.showstmts = activityDef.getParams().getOptionalBoolean("showstatements").orElse(false);
        this.fileName = activityDef.getParams().getOptionalString("filename").orElse("stdout");
        this.stmtsDocList = StatementsLoader.load(logger, yaml_loc, interp, "activities");
    }

    @Override
    public void shutdownActivity() {
        try {
            if (pw!=null) {
                pw.close();
            }
        } catch (Exception e) {
            logger.warn("error closing writer:" + e, e);
        }
    }

    @Override
    public void initActivity() {
        logger.debug("initializing activity: " + this.activityDef.getAlias());
        exceptionMeterMetrics = new ExceptionMeterMetrics(activityDef);

        onActivityDefUpdate(activityDef);

        opSequence = initOpSequencer();
        setDefaultsFromOpSequence(opSequence);

        bindTimer = ActivityMetrics.timer(activityDef, "bind");
        executeTimer = ActivityMetrics.timer(activityDef, "execute");
        resultTimer = ActivityMetrics.timer(activityDef, "result");
        triesHisto = ActivityMetrics.histogram(activityDef, "tries");

        this.pw = createPrintWriter();

    }

    protected Writer createPrintWriter() {
        PrintWriter pw;
        if (fileName.toLowerCase().equals("stdout")) {
            pw = new PrintWriter(System.out);
        } else {
            try {
                pw = new PrintWriter(fileName);
                pw.print("");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Error initializing printwriter:" + e, e);
            }
        }
        return pw;
    }

    private OpSequence<StringBindings> initOpSequencer() {
        //List<StringBindingsTemplate> stringBindingsTemplates = new ArrayList<>();
        SequencerType sequencerType = SequencerType.valueOf(
                getParams().getOptionalString("seq").orElse("bucket")
        );
        SequencePlanner<StringBindings> sequencer = new SequencePlanner<>(sequencerType);

        String tagfilter = activityDef.getParams().getOptionalString("tags").orElse("");
        List<StmtDef> stmts = stmtsDocList.getStmts(tagfilter);

        String format = getParams().getOptionalString("format").orElse(null);

        if ((stmts.size()==0 && stmtsDocList.getDocBindings().size() > 0) || format!=null) {
            logger.info("Creating stdout statement template from bindings, since none is otherwise defined.");
            String generatedStmt = genStatementTemplate(stmtsDocList.getDocBindings().keySet());
            BindingsTemplate bt = new BindingsTemplate();
            stmtsDocList.getDocBindings().forEach(bt::addFieldBinding);
            StringBindingsTemplate sbt = new StringBindingsTemplate(generatedStmt, bt);
            StringBindings sb = sbt.resolve();
            sequencer.addOp(sb,1L);
        } else if (stmts.size() > 0) {
            for (StmtDef stmt : stmts) {
                ParsedStmt parsed = stmt.getParsed().orError();
                BindingsTemplate bt = new BindingsTemplate(parsed.getBindPoints());
                String statement = parsed.getPositionalStatement(Function.identity());
                Objects.requireNonNull(statement);
                if (!statement.endsWith("\n") && getParams().getOptionalBoolean("newline").orElse(true)) {
                    statement = statement+"\n";
                }

                StringBindingsTemplate sbt = new StringBindingsTemplate(stmt.getStmt(), bt);
                StringBindings sb = sbt.resolve();
                sequencer.addOp(sb,Long.valueOf(stmt.getParams().getOrDefault("ratio","1")));
            }
        } else {
            logger.error("Unable to create a stdout statement if you have no active statements or bindings configured.");
        }

        OpSequence<StringBindings> opSequence = sequencer.resolve();
        return opSequence;
    }

    private String genStatementTemplate(Set<String> keySet) {
        TemplateFormat format = getParams().getOptionalString("format")
                .map(TemplateFormat::valueOf)
                .orElse(TemplateFormat.assignments);
        boolean ensureNewline = getParams().getOptionalBoolean("newline")
                .orElse(true);
        String stmtTemplate = format.format(ensureNewline,new ArrayList<>(keySet));
        return stmtTemplate;
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);

        ParameterMap params = activityDef.getParams();
        this.retry_delay = params.getOptionalInteger("retry_delay").orElse(1000);
        this.retries = params.getOptionalInteger("retries").orElse(3);
    }

    public synchronized void write(String statement) {
        int tries = 0;
        Exception e = null;
        while (tries < retries) {
            tries++;
            if (pw == null) {
                pw = createPrintWriter();
            }
            try {
                pw.write(statement);
                pw.flush();
                return;
            } catch (Exception error) {
                logger.warn("Error during write:" + error, error);
                if (retry_delay > 0) {
                    try {
                        Thread.sleep(retry_delay);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
        throw new RuntimeException("Retries exhausted: " + tries + "/" + retries);
    }

    public Boolean getShowstmts() {
        return showstmts;
    }
}
