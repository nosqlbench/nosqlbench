package io.nosqlbench.adapter.cqld4;

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


public class UndefinedResultSetException extends RuntimeException {
    private final Cqld4Op cqld4op;

    public UndefinedResultSetException(Cqld4Op cqld4Op) {
        this.cqld4op = cqld4Op;
    }

    @Override
    public String getMessage() {
        return "Attempted to access a result set which was not defined in op " + cqld4op.toString();
    }
}
