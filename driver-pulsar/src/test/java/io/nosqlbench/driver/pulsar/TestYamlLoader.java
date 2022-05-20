package io.nosqlbench.driver.pulsar;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


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
