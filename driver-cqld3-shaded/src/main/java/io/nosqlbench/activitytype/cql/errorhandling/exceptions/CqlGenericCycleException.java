package io.nosqlbench.activitytype.cql.errorhandling.exceptions;

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

    private final long cycle;

    public CqlGenericCycleException(long cycle, Throwable cause) {
        super(cause);
        this.cycle = cycle;
    }

    public CqlGenericCycleException(long cycle, String message) {
        super(message);
        this.cycle = cycle;
    }

    public CqlGenericCycleException(long cycle, String message, Throwable cause) {
        super(message, cause);
        this.cycle = cycle;
    }

    public CqlGenericCycleException(long cycle) {
        super();
        this.cycle = cycle;
    }

    @Override
    public String getMessage() {
        return "cycle:" + cycle + " caused by:" + super.getMessage();
    }

    public long getCycle() {
        return cycle;
    }



}
