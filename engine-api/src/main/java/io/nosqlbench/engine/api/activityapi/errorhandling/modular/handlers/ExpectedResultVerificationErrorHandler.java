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

package io.nosqlbench.engine.api.activityapi.errorhandling.modular.handlers;

import io.nosqlbench.api.errors.ResultMismatchError;
import io.nosqlbench.engine.api.activityapi.errorhandling.ErrorMetrics;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import io.nosqlbench.engine.api.metrics.ExceptionExpectedResultVerificationMetrics;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorHandler;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

/**
 * The expected result verification error handler will create, if needed, two metric
 * objects for error and retry counts.
 */
@Service(value = ErrorHandler.class, selector = "verifyexpected")
public class ExpectedResultVerificationErrorHandler implements ErrorHandler, ErrorMetrics.Aware {
    private static final Logger logger = LogManager.getLogger("VERIFY");
    private ExceptionExpectedResultVerificationMetrics exceptionExpectedResultVerificationMetrics;

    @Override
    public ErrorDetail handleError(String name, Throwable t, long cycle, long durationInNanos, ErrorDetail detail) {
        if (t instanceof ResultMismatchError erve) {
            if (erve.getTriesLeft() == 0) {
                logger.warn("Cycle: {} Verification of result did not pass following expression: {}", cycle, erve.getExpressionDetails());
                exceptionExpectedResultVerificationMetrics.countVerificationErrors();
            } else {
                logger.info("Cycle: {} Verification of result did not pass. {} retries left.", cycle, erve.getTriesLeft());
                exceptionExpectedResultVerificationMetrics.countVerificationRetries();
            }
        }
        return detail;
    }

    @Override
    public void setErrorMetricsSupplier(Supplier<ErrorMetrics> supplier) {
        this.exceptionExpectedResultVerificationMetrics = supplier.get().getExceptionExpectedResultVerificationMetrics();
    }

}
