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

package io.nosqlbench.virtdata.core.bindings;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

public class ResolverDiagnostics {

    private final static Logger logger  = LogManager.getLogger(ResolverDiagnostics.class);

    private ResolvedFunction resolvedFunction;
    private final StringBuilder log = new StringBuilder();
    private Throwable error;

    public ResolverDiagnostics() {
    }

    public <T> Optional<DataMapper<T>> getOptionalMapper() {
        return Optional.ofNullable(resolvedFunction).map(ResolvedFunction::getFunctionObject).map(DataMapperFunctionMapper::map);

    }

    public Optional<ResolvedFunction> getResolvedFunction() {
        return Optional.ofNullable(getResolvedFunctionOrThrow());
    }

    public ResolvedFunction getResolvedFunctionOrThrow() {
        if (error!=null) {
            throw new RuntimeException(error.getMessage(),error);
        }
        return resolvedFunction;
    }

    public ResolverDiagnostics error(Exception e) {
        this.error = e;
        log.append("ERROR encountered while resolving function:\n");
        log.append(e.toString()).append("\n");
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        e.printStackTrace(pw);
        String stacktrace = writer.toString();

        log.append("stack trace:\n");
        log.append(stacktrace);
        return this;
    }

    public ResolverDiagnostics setResolvedFunction(ResolvedFunction resolvedFunction) {
        this.resolvedFunction = resolvedFunction;
        return this;
    }

    public ResolverDiagnostics trace(String s) {
        logger.trace(s);
        log.append(s).append("\n");
        return this;
    }

    public String toString() {
        return log.toString();
    }

}
