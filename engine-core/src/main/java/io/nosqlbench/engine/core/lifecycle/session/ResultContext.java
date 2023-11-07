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

package io.nosqlbench.engine.core.lifecycle.session;

import io.nosqlbench.engine.core.lifecycle.ExecutionResult;

import java.util.function.Consumer;

public class ResultContext implements AutoCloseable {
    private final Consumer<ResultContext> receiver;
    private ExecutionResult.Status status;

    public ResultContext(Consumer<ResultContext> receiver) {
        this.receiver = receiver;
    }

    private final long startMillis = System.currentTimeMillis();
    private Exception error;
    public final StringBuilder buf = new StringBuilder();
    private long stopMillis;

    public void error(Exception error) {
        this.error = error;
    }

    public void output(CharSequence cs) {
        buf.append(cs);
    }
    public String output() {
        return buf.toString();
    }

    @Override
    public void close() throws RuntimeException {
        this.stopMillis = System.currentTimeMillis();
        if (this.status==null) {
            this.status= ExecutionResult.Status.ERROR;
            if (this.error!=null) {
                this.error=new RuntimeException("early execution result with no asserted status. Call setStatus on your result context or end with `return ctx.ok() or ctx.error(...)`");
            }
        }
        receiver.accept(this);
    }

    public ExecutionResult toExecutionResult() {
        return new ExecutionResult(this.startMillis,this.stopMillis,buf.toString(), error);
    }

    public void ok() {
        this.status= ExecutionResult.Status.OK;
    }
    public void error() {
        this.status= ExecutionResult.Status.ERROR;
    }

    public long startMillis() {
        return this.startMillis;
    }

    public long stopMillis() {
        return this.stopMillis;
    }

    public Exception getException() {
        return error;
    }
}
