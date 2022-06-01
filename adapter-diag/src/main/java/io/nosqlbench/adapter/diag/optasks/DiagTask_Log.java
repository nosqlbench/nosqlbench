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

import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.config.standard.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Service(value=DiagOpTask.class,selector="log")
public class DiagTask_Log implements DiagOpTask, NBConfigurable {
    private final static Logger logger = LogManager.getLogger("DIAG");
    private Level level;

    @Override
    public Map<String, Object> apply(Long aLong, Map<String, Object> stringObjectMap) {
        logger.log(level,"cycle=" + aLong+" state="+stringObjectMap.toString());
        return stringObjectMap;
    }

    @Override
    public void applyConfig(NBConfiguration cfg) {
        String level = cfg.getOptional("level").orElse("INFO");
        this.level = Level.valueOf(level);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return ConfigModel.of(DiagTask_Log.class)
            .add(Param.optional("level"))
            .asReadOnly();
    }
}
