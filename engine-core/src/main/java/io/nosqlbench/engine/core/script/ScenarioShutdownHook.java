package io.nosqlbench.engine.core.script;

import org.apache.logging.log4j.Logger;

public class ScenarioShutdownHook extends Thread {

    private final Scenario scenario;
    private final Logger logger;

    public ScenarioShutdownHook(Scenario scenario) {
        this.scenario = scenario;
        logger = scenario.getLogger();
    }

    @Override
    public void run() {
        scenario.finish();
    }

}
