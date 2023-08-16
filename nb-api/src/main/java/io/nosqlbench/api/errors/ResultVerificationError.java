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

package io.nosqlbench.api.errors;

import java.io.Serializable;

public class ResultVerificationError extends RuntimeException {
    private final int triesLeft;
    private final String expressionDetails;

    public ResultVerificationError(String message, int triesLeft, String expressionDetails) {
        super("Error while verifying result with " + triesLeft + " tries remaining: " + message);
        this.triesLeft = triesLeft;
        this.expressionDetails = expressionDetails;
    }

    public ResultVerificationError(Throwable throwable, int triesLeft, String expressionDetails) {
        super("Error while verifying result with " + triesLeft + " tries remaining: " + throwable.getMessage(),throwable);
        this.triesLeft = triesLeft;
        this.expressionDetails = expressionDetails;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public int getTriesLeft() {
        return triesLeft;
    }

    public Serializable getExpressionDetails() {
        return expressionDetails;
    }

}
