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

package io.nosqlbench.engine.api.activityapi.errorhandling.modular;

public class ErrorDetail {
    public final Retry retryable;
    public final int resultCode;
    public final String name;

    public boolean isRetryable() {
        return retryable == Retry.DoRetry;
    }

    public enum Retry {
        DoNotRetry,
        DoRetry,
        Unset
    }

    public ErrorDetail(String name, Retry retryable, int resultCode) {
        this.name = name;
        this.resultCode = resultCode;
        this.retryable = retryable;
    }

    public ErrorDetail withResultCode(int resultCode) {
        if (this.resultCode == resultCode) {
            return this;
        }
        return new ErrorDetail(name, this.retryable, resultCode);
    }

    public ErrorDetail withRetryable() {
        if (this.retryable == Retry.DoRetry) {
            return this;
        }
        return new ErrorDetail(name, Retry.DoRetry, this.resultCode);
    }

    public static ErrorDetail OK = new ErrorDetail("OK",Retry.Unset, 0);
    public static ErrorDetail ERROR_NONRETRYABLE = new ErrorDetail("ERROR_NONRETRYABLE",Retry.DoNotRetry, 127);
    public static ErrorDetail ERROR_RETRYABLE = new ErrorDetail("ERROR_RETRYABLE",Retry.DoRetry, 127);
    public static ErrorDetail ERROR_UNKNOWN = new ErrorDetail("ERROR_UNKNOWN",Retry.Unset, 127);
}
