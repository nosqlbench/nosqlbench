/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.engine.api.metrics;

import com.codahale.metrics.Counter;
import io.nosqlbench.nb.api.components.core.NBComponent;


/**
 * Use this to provide exception metering during expected result verification.
 */
public class ExceptionExpectedResultVerificationMetrics {
    private final NBComponent parent;
    private final Counter verificationErrors;
    private final Counter verificationRetries;

    public ExceptionExpectedResultVerificationMetrics(final NBComponent parent) {
        this.parent = parent;
        this.verificationRetries=parent.create().counter("verificationcounts_RETRIES");
        this.verificationErrors=parent.create().counter( "verificationcounts_ERRORS");
    }

    public void countVerificationRetries() {
        verificationRetries.inc();
    }

    public void countVerificationErrors() {
        verificationErrors.inc();
    }

    public Counter getVerificationErrors() {
        return verificationErrors;
    }

    public Counter getVerificationRetries() {
        return verificationRetries;
    }
}
