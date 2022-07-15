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

package io.nosqlbench.adapter.diag.optasks;

import io.nosqlbench.api.config.standard.*;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.config.standard.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Service(value= DiagTask.class,selector="log")
public class DiagTask_log implements DiagTask, NBConfigurable {
    private final static Logger logger = LogManager.getLogger("DIAG");
    private Level level;
    private long modulo;
    private long interval;
    private String name;

    @Override
    public Map<String, Object> apply(Long aLong, Map<String, Object> stringObjectMap) {
        if ((aLong % modulo) == 0) {
            logger.log(level,"cycle=" + aLong+" state="+stringObjectMap.toString());
        }
        return stringObjectMap;
    }

    @Override
    public void applyConfig(NBConfiguration cfg) {
        String level = cfg.getOptional("level").orElse("INFO");
        this.name = cfg.get("name");
        this.level = Level.valueOf(level);
        this.modulo = cfg.get("modulo",long.class);
        this.interval = cfg.get("interval",long.class);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(DiagTask_log.class)
            .add(Param.required("name",String.class))
            .add(Param.optional("level"))
            .add(Param.defaultTo("modulo", 1))
            .add(Param.defaultTo("interval",1000))
            .asReadOnly();
    }

    @Override
    public String getName() {
        return name;
    }
}
