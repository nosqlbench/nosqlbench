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

package io.nosqlbench.analysis;

import io.nosqlbench.api.apps.BundledApp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

//@Service(value=BundledApp.class, selector = "cql-ring-analyzer")
public class RingAnalyzer implements BundledApp {
    private final static Logger logger = LogManager.getLogger(RingAnalyzer.class);

    @Override
    public int applyAsInt(String[] args) {
        RingAnalyzerConfig cfg = new RingAnalyzerConfig();
        CommandLine cli = new CommandLine(cfg);
        CommandLine.ParseResult cl = cli.parseArgs(args);

        logger.info(() -> "filename: " + cfg.filename);
        return 0;
    }
}
