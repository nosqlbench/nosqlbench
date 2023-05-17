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

public class ExpectedResultVerificationError extends RuntimeException {
    private final int triesLeft;
    private final Serializable expectedResultExpression;
    private final Object result;

    public ExpectedResultVerificationError(int triesLeft, Serializable expectedResultExpression, Object result) {
        this.triesLeft = triesLeft;
        this.expectedResultExpression = expectedResultExpression;
        this.result = result;
    }

    public int getTriesLeft() {
        return triesLeft;
    }

    public Serializable getExpectedResultExpression() {
        return expectedResultExpression;
    }

    public String getResultAsString() {
        return result.toString(); // TODO JK how to traverse the first x characters of the result? parse to json? via reflection?
    }
}
