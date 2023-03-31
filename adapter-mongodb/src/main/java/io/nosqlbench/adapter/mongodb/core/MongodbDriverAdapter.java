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

package io.nosqlbench.adapter.mongodb.core;

import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.Param;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

/**
 * Special thanks to Justin Chu who authored the original NoSQLBench MongoDB ActivityType.
 */
@Service(value = DriverAdapter.class, selector = "mongodb")
public class MongodbDriverAdapter extends BaseDriverAdapter<Op, MongoSpace> {

    private static final Logger logger = LogManager.getLogger(MongodbDriverAdapter.class);
    private String connection;

    @Override
    public OpMapper<Op> getOpMapper() {
        return new MongoOpMapper(this);
    }

    @Override
    public Function<String, ? extends MongoSpace> getSpaceInitializer(NBConfiguration cfg) {
        return s -> new MongoSpace(s, this.connection, cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {

        NBConfigModel mongoConfigModel = MongoSpace.getConfigModel();
        NBConfigModel parentModel = super.getConfigModel();
        final NBConfigModel merged = mongoConfigModel.add(parentModel);

        if (logger.isDebugEnabled()) {
            logger.debug("mongo params:");
            for (Param<?> param : mongoConfigModel.getParams()) {
                logger.debug("param: {}", param);
            }

            logger.debug("parent params:");
            for (Param<?> param : parentModel.getParams()) {
                logger.debug("param: {}", param);
            }

            logger.debug("merged params:");
            for (Param<?> param : merged.getParams()) {
                logger.debug("param: {}", param);
            }
        }
        return merged;
    }

    public void setConnection(String connection) {
        logger.debug("\n\n\n\nSetting connection: {}", connection);
        this.connection = connection;
    }
}
