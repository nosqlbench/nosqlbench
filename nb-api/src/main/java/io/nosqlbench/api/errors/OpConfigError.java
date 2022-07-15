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

package io.nosqlbench.api.errors;

/**
 * OpConfigErrors are {@link BasicError}s which are known to occur when
 * there is an invalid set of configuration details for an op. This can
 * occur when the user-provided op template data (such as from YAML)
 * is being inspected by a driver adapter to synthesize operations
 * (or functions which know how to do so).
 */
public class OpConfigError extends ActivityInitError {
    private final String configSource;

    public OpConfigError(String error) {
        this(error,null,null);
    }
    public OpConfigError(String error, Throwable cause) {
        this(error, null, cause);
    }

    public OpConfigError(String error, String configSource) {
        super(error);
        this.configSource = configSource;
    }


    public OpConfigError(String error, String configSource, Throwable cause) {
        super(error,cause);
        this.configSource = configSource;
    }

    public String getCfgSrc() {
        return configSource;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("Error while configuring op from workload template:");

        String cfgsrc = configSource;
        if (cfgsrc==null) {
            Throwable cause = getCause();
            while (cause instanceof OpConfigError && cfgsrc==null) {
                cfgsrc = ((OpConfigError) cause).getCfgSrc();
                cause = cause.getCause();
            }
        }

        if (cfgsrc!=null) {
            sb.append(" [from:" + configSource + "] ");
        }

        if (getCause()!=null) {
            sb.append(" cause: " + getCause().getClass().getSimpleName() + ":"+ getCause().getMessage());
        }

//        if (getCause()!=null) {
//            StackTraceElement causeFrame = getCause().getStackTrace()[0];
//            sb.append("\n\t caused by ")
//                .append(getCause().getMessage())
//                .append("\n\t at (")
//                .append(causeFrame.getFileName())
//                .append(":")
//                .append(causeFrame.getLineNumber())
//                .append(")");
//        }

        if (super.getMessage()!=null) {
            sb.append(" " + super.getMessage());
        }

        return sb.toString();
    }
}
