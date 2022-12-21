/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.engine.rest.services.openapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

public class OpenApiLoaderTest {
    private final static Logger logger = LogManager.getLogger(OpenApiLoaderTest.class);
    @Test
    public void testYamlGenerator() {
        String openidpath = "stargate.yaml";
        String filterJson = "{\n" +
            "'POST /api/rest/v1/auth' : {}\n" +
            "}\n";

        String result = OpenApiLoader.generateWorkloadFromFilepath(openidpath, filterJson);
        logger.debug(result);

    }

}
