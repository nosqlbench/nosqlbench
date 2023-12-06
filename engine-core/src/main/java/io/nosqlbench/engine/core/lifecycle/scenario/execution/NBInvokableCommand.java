/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.engine.core.lifecycle.scenario.execution;

import io.nosqlbench.engine.core.lifecycle.scenario.context.NBBufferedCommandContext;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBCommandParams;
import io.nosqlbench.nb.api.components.NBBaseComponent;
import io.nosqlbench.nb.api.components.NBComponent;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.BiFunction;

public abstract class NBInvokableCommand extends NBBaseComponent implements BiFunction<NBBufferedCommandContext, NBCommandParams, Object> {
    private static final Logger logger = LogManager.getLogger(NBInvokableCommand.class);

    public NBInvokableCommand(NBBufferedCommandContext parentComponent, NBLabels componentSpecificLabelsOnly) {
        super(parentComponent, componentSpecificLabelsOnly);
    }

    @Override
    public abstract Object apply(NBBufferedCommandContext nbBufferedCommandContext, NBCommandParams nbCommandParams);

    public NBCommandResult invokeSafe(NBBufferedCommandContext context, NBCommandParams params) {
        Object resultObject = null;
        Exception exception = null;
        long startAt = System.currentTimeMillis();
        NBCommandResult result = null;
        try {
            logger.debug("invoking command: " + this);
            resultObject=apply(context, params);
            logger.debug("cmd produced: " + (resultObject==null ? "NULL" : resultObject.toString()));
        } catch (Exception e) {
            exception = e;
            logger.error("error in command (stack trace follows): " + this.description() + ": " + exception);
            exception.printStackTrace(System.out);
        } finally {
            long endAt = System.currentTimeMillis();
            result = new NBCommandResult(context, startAt, endAt, exception);
            if (resultObject!=null) {
                result.setResultObject(resultObject);
            }
        }
        return result;
    }

}
