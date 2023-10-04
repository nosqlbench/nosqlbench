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

import io.nosqlbench.engine.core.lifecycle.scenario.context.NBSceneBuffer;

public class ScenarioResult {
    private final long startedAt;
    private final long endedAt;
    private final String iolog;
    private final Exception error;

    public ScenarioResult(long startedAt, long endedAt, String iolog, Exception error) {
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.iolog = iolog;
        this.error = error;
    }

    public ScenarioResult(ScenarioResult baseResult, NBSceneBuffer bufferedContext) {
        this.startedAt = baseResult.startedAt;
        this.endedAt = baseResult.endedAt;
        String log = bufferedContext.getIoLog();
        this.error = baseResult.error;
        if (this.error!=null) {
            log+=error.getMessage();
        }
        this.iolog = log;

    }

    public Exception getException() {
        return error;
    }

    public String getIOLog() {
        return iolog;
    }

    @Override
    public String toString() {
        return ((error!=null)? "ERROR:" + error.toString() : "") +
        getIOLog();
    }
}
