package io.nosqlbench.nb.api.errors;

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


/**
 * User exceptions are errors for which we know how to explain the cause to the user.
 * For these, we should not need to log or report stack traces to any channel, as
 * the cause of and thus the remedy for the error should be very obvious.
 */
public class BasicError extends RuntimeException {
    public BasicError(String exception) {
        super(exception);
    }
    public BasicError(String exception, Throwable cause) {
        super(exception,cause);
    }
}
