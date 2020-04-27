package io.nosqlbench.driver.webdriver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.driver.webdriver.side.Command;
import io.nosqlbench.driver.webdriver.side.SideConfig;
import io.nosqlbench.driver.webdriver.side.Test;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityapi.planning.SequencePlanner;
import io.nosqlbench.engine.api.activityapi.planning.SequencerType;
import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtDef;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.templating.StrInterpolator;
import io.nosqlbench.nb.api.content.NBIO;
import io.nosqlbench.nb.api.errors.BasicError;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WebDriverActivity extends SimpleActivity {

    private final static Logger logger = LoggerFactory.getLogger(WebDriverActivity.class);

    //    private final StmtsDocList stmtsDocList;
    private OpSequence<CommandTemplate> opSequence;

    private ConcurrentHashMap<Integer,WebContext> contexts = new ConcurrentHashMap<>();
//    private static ThreadLocal<WebContext> TL_WebContext = new ThreadLocal<>();

    public WebDriverActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public void initActivity() {
        super.initActivity();
        this.opSequence = initOpSequence();
        onActivityDefUpdate(activityDef);
        setDefaultsFromOpSequence(opSequence);
        int threads = getActivityDef().getThreads();
        logger.info("pre-initializing web browsers");
        for (int i = 0; i < threads; i++) {
            getWebContext(i);
        }
    }

    private OpSequence<CommandTemplate> initOpSequence() {
        OpSequence<CommandTemplate> sequence;

        String yaml_loc = getParams().getOptionalString("yaml","workload").orElse(null);
        String side_loc = getParams().getOptionalString("side").orElse(null);

        if (yaml_loc == null && side_loc == null) {
            throw new BasicError("You must provide yaml= or side=, but neither was found");
        }
        if (yaml_loc != null && side_loc != null) {
            throw new BasicError("You must provide either yaml= or side=, but not both.");
        }
        if (yaml_loc != null) {
            sequence=initOpSequenceFromYaml();
        } else {
            sequence=initOpSequenceFromSide();
        }

        if (sequence.getSequence().length==0) {
            logger.warn("The sequence contains zero operations.");
        }
        return sequence;

    }

    private OpSequence<CommandTemplate> initOpSequenceFromSide() {

        Optional<String> sideFile = activityDef.getParams().getOptionalString("side");
        String sideToImport = sideFile.get();
        String side = NBIO.all().name(sideToImport).extension("side").one().asString();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        SideConfig sc = gson.fromJson(side, SideConfig.class);
        String sideName = sc.getName();
        String sideUrl = sc.getUrl();

        LinkedHashMap<String,String> commands = new LinkedHashMap<>();
        for (Test test : sc.getTests()) {
            int idx=0;
            StringBuilder testBuilder = new StringBuilder();
            for (Command command : test.getCommands()) {
                StringBuilder cmdBuilder = new StringBuilder();
                String cmd = command.getCommand();
                cmdBuilder.append(cmd);
                if (command.getCommand().equals("open")) {
                    cmdBuilder.append(" url='").append(sideUrl.replaceAll("'","\\\\'")).append("'");
                }

                if (command.getTarget() != null && !command.getTarget().isEmpty()) {
                    String tg=null;
                    if (command.getTargets().size()>0) {
                        for (int i = 0; i < command.getTargets().size(); i++) {
                            List<String> strings = command.getTargets().get(i);
                            if (strings.get(1).equals("xpath:idRelative")) {
                                logger.debug("favoring xpath form of selector:" + strings.get(0));
                                tg=strings.get(0);
                            }
                        }
                    }
                    if (tg==null) {
                        tg = command.getTarget();
                    }
                    cmdBuilder.append(" target='").append(tg.replaceAll("'","\\\\'")).append("'");

                }
                if (command.getValue() != null && !command.getValue().isEmpty()) {
                    cmdBuilder.append(" value='").append(command.getValue().replaceAll("'","\\\\'")).append("'");
                }
                cmdBuilder.append("\n");
                logger.debug("build cmd: '" + testBuilder.toString() + "'");
                String cmdName = sideName+"_"+String.format("%s_%03d", sideName, ++idx);
                commands.put(cmdName,cmdBuilder.toString());
            }
        }

        String export = activityDef.getParams().getOptionalString("export").orElse(null);
        if (export!=null) {
            Path exportTo = Path.of(export);
            if (Files.exists(exportTo)) {
                throw new BasicError("File exists: " + exportTo + ", remove it first.");
            }
            try {
                StringBuilder stmts = new StringBuilder();
                stmts.append("statements:\n");
                commands.forEach((name, cmd)-> {
                    System.out.println("name: " + name);
                    System.out.println("cmd: " +  cmd);
                    stmts.append("  - ").append(name).append(": ").append(cmd);
                });
                Files.writeString(exportTo,stmts.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            logger.info("completed importing from " + sideFile + " to " + exportTo);
            System.exit(0);
        }

        SequencerType sequencerType = getParams()
            .getOptionalString("seq")
            .map(SequencerType::valueOf)
            .orElse(SequencerType.bucket);
        SequencePlanner<CommandTemplate> planner = new SequencePlanner<>(sequencerType);
        commands.forEach((name,cmd) -> {
            CommandTemplate commandTemplate = new CommandTemplate(cmd, Map.of(), name, false);
            planner.addOp(commandTemplate,(c) -> 1L);
        });
        OpSequence<CommandTemplate> sequence = planner.resolve();
        return sequence;
    }

    private OpSequence<CommandTemplate> initOpSequenceFromYaml() {
        StrInterpolator interp = new StrInterpolator(activityDef);
        String yaml_loc = activityDef.getParams().getOptionalString("yaml", "workload").orElse("default");
        StmtsDocList stmtsDocList = StatementsLoader.load(logger, yaml_loc, interp, "activities");

        SequencerType sequencerType = getParams()
            .getOptionalString("seq")
            .map(SequencerType::valueOf)
            .orElse(SequencerType.bucket);
        SequencePlanner<CommandTemplate> planner = new SequencePlanner<>(sequencerType);

        String tagfilter = activityDef.getParams().getOptionalString("tags").orElse("");
        List<StmtDef> stmts = stmtsDocList.getStmts(tagfilter);

        if (stmts.size() == 0) {
            throw new BasicError("There were no active statements with tag filter '" + tagfilter + "'");
        }

        for (StmtDef optemplate : stmts) {
            long ratio = Long.parseLong(optemplate.getParams().getOrDefault("ratio", "1"));
            CommandTemplate cmd = new CommandTemplate(optemplate, false);
            planner.addOp(cmd, ratio);
        }
        return planner.resolve();
    }

    public OpSequence<CommandTemplate> getOpSequence() {
        return opSequence;
    }


    public synchronized WebContext getWebContext(int slot) {
        try {
            WebContext context = contexts.get(slot);
            if (context == null) {
                logger.info("initializing chromedriver for thread " + slot);
                System.setProperty("webdriver.http.factory", "okhttp");
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.setHeadless(activityDef.getParams().getOptionalBoolean("headless").orElse(false));
                WebDriver webdriver = new ChromeDriver(chromeOptions);
                context = new WebContext(webdriver);
                contexts.put(slot, context);
            } else {
                logger.info("using cached chromedriver for thread " + slot);
            }
            return context;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void shutdownActivity() {
        contexts.forEach((s, d) -> {
            logger.debug("closing driver for thread " + s);
            d.driver().close();
        });
    }
}
