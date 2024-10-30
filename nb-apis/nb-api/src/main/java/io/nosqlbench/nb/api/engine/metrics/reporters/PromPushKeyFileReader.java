/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.api.engine.metrics.reporters;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public class PromPushKeyFileReader implements Supplier<String> {
    private final static Logger logger = LogManager.getLogger("METRICS" );

    private final Path keyfilePath;

    public PromPushKeyFileReader(Path path) {
        this.keyfilePath = path;
    }

    public PromPushKeyFileReader(String sourcePath) {
        this.keyfilePath = Path.of(sourcePath);
    }

    @Override
    public String get() {
        if (!Files.exists(keyfilePath)) {
            logger.warn("apikeyfile does not exist at '" + keyfilePath);
            return null;
        } else {
            try {
                String apikey = Files.readString(keyfilePath, StandardCharsets.UTF_8);
                apikey = apikey.trim();
                return apikey;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
