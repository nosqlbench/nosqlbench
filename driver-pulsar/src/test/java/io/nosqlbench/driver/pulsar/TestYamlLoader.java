package io.nosqlbench.driver.pulsar;

import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtsDocList;
import io.nosqlbench.engine.api.activityconfig.rawyaml.RawStmtsLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

public class TestYamlLoader {

    private final static Logger logger = LogManager.getLogger(TestYamlLoader.class);

    @Test
    public void loadAvroYaml() {
        RawStmtsLoader sl = new RawStmtsLoader();
        RawStmtsDocList rsdl = sl.loadPath(logger, "activities/pulsar_client_avro.yaml");
    }
}
