package io.nosqlbench.adapter.cqld4.exceptions;

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


public abstract class CqlGenericCycleException extends RuntimeException {

    public CqlGenericCycleException(Throwable cause) {
        super(cause);
    }

    public CqlGenericCycleException(String message) {
        super(message);
    }

    public CqlGenericCycleException(String message, Throwable cause) {
        super(message, cause);
    }

}
