package io.nosqlbench.activitytype.cql.errorhandling;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.activitytype.cql.api.ErrorResponse;

public class ErrorStatus {
    private final boolean retryable;
    private int resultCode;
    private final ErrorResponse response;

    public ErrorStatus(ErrorResponse response, boolean retryable, int resultCode) {
        this.response = response;
        this.retryable = retryable;
        this.resultCode = resultCode;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public ErrorResponse getResponse() {
        return response;
    }
}
