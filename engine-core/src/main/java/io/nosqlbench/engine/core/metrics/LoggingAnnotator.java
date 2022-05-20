package io.nosqlbench.engine.core.metrics;

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


import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.annotations.Annotation;
import io.nosqlbench.nb.api.annotations.Annotator;
import io.nosqlbench.nb.api.config.standard.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

@Service(value = Annotator.class, selector = "log")
public class LoggingAnnotator implements Annotator, NBMapConfigurable {

    private final static Logger annotatorLog = LogManager.getLogger("ANNOTATION");
    private Level level;

    private final Map<String, String> tags = new LinkedHashMap<>();

    public LoggingAnnotator() {
    }

    @Override
    public void recordAnnotation(Annotation annotation) {
        String inlineForm = annotation.asJson();
        annotatorLog.log(level, inlineForm);
    }

    @Override
    public void applyConfig(Map<String, ?> providedConfig) {
        NBConfigModel configModel = getConfigModel();
        NBConfiguration cfg = configModel.apply(providedConfig);
        String levelName = cfg.get("level");
        this.level = Level.valueOf(levelName);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(this.getClass())
            .add(Param.defaultTo("level", "INFO")
                .setDescription("The logging level to use for this annotator"))
            .asReadOnly();
    }

}
