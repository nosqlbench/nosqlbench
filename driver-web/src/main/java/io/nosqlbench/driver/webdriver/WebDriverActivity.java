package io.nosqlbench.driver.webdriver;

import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityapi.planning.SequencePlanner;
import io.nosqlbench.engine.api.activityapi.planning.SequencerType;
import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtDef;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.templating.StrInterpolator;
import io.nosqlbench.nb.api.errors.BasicError;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class WebDriverActivity extends SimpleActivity {

    private final static Logger logger = LoggerFactory.getLogger(WebDriverActivity.class);

    private final StmtsDocList stmtsDocList;
    private OpSequence<CommandTemplate> opSequence;

    private static ThreadLocal<WebContext> TL_WebContext = new ThreadLocal<>();
    private ConcurrentHashMap<Integer,WebContext> contexts = new ConcurrentHashMap<>();

    public WebDriverActivity(ActivityDef activityDef) {
        super(activityDef);
        StrInterpolator interp = new StrInterpolator(activityDef);
        String yaml_loc = activityDef.getParams().getOptionalString("yaml", "workload").orElse("default");
        this.stmtsDocList = StatementsLoader.load(logger, yaml_loc, interp, "activities");
    }

    @Override
    public void initActivity() {
        super.initActivity();
        this.opSequence = initOpSequence();
        onActivityDefUpdate(activityDef);
    }

    private OpSequence<CommandTemplate> initOpSequence() {
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
            CommandTemplate cmd = new CommandTemplate(optemplate);
            planner.addOp(cmd, ratio);
        }
        return planner.resolve();
    }

    public OpSequence<CommandTemplate> getOpSequence() {
        return opSequence;
    }


    public synchronized WebContext getWebContext(int slot) {
        try {
            WebContext context = TL_WebContext.get();
            if (context == null) {
                logger.info("initializing chromedriver for thread " + slot);
                System.setProperty("webdriver.http.factory", "okhttp");
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.setHeadless(activityDef.getParams().getOptionalBoolean("headless").orElse(false));
                WebDriver webdriver = new ChromeDriver(chromeOptions);
                context = new WebContext(webdriver);
                contexts.put(slot,context);
                TL_WebContext.set(context);
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
            d.driver().quit();
        });
    }
}
