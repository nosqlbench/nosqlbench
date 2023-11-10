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

import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBBufferedScenarioContext;

import java.util.function.Supplier;

public class ScenarioPhaseResult implements Supplier<ExecutionResult> {
    private final long startedAt;
    private final long endedAt;
    private final Exception exception;
    private final NBBufferedScenarioContext fixtures;

    public ScenarioPhaseResult(NBBufferedScenarioContext fixtures, long start, long end, Exception exception) {
        this.fixtures = fixtures;
        this.startedAt=start;
        this.endedAt=end;
        this.exception =exception;
    }

    public Exception getException() {
        return this.exception;
    }

    public void report() {
        System.out.println(getIOLog());
        if (exception!=null) {
            if (exception instanceof RuntimeException rte) {
                throw rte;
            } else {
                throw new RuntimeException(exception);
            }
        }
    }
    public void exitWithCode() {
        System.out.print(getIOLog());
        if (exception!=null) {
            System.exit(2);
        }
    }

    public String getIOLog() {
        return fixtures.getIOLog();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ScenarioResult [")
            .append(this.endedAt - this.startedAt)
            .append("ms]");
        if (exception==null) {
             sb.append(" OK ");
        } else {
            sb.append(" ERROR ").append(exception);
        }
        String iolog = getIOLog();
        if (!iolog.isEmpty()) {
            sb.append(" IO{\n").append("}\n").append(iolog);
        }
        return sb.toString();
    }

    @Override
    public ExecutionResult get() {
        return new ExecutionResult(this.startedAt,this.endedAt,this.getIOLog(),this.exception);
    }
}
