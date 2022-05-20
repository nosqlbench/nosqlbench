package io.nosqlbench.virtdata.lang.ast;

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


public class Expression {

    private Assignment assignment;
    private FunctionCall call;

    public Expression() {
    }

    public Expression(Assignment assignment, FunctionCall call) {
        this.assignment = assignment;
        this.call = call;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public FunctionCall getCall() {
        return call;
    }

    public void setCall(FunctionCall call) {
        this.call = call;
    }

    @Override
    public String toString() {
        return (assignment != null ? assignment + "=" : "") + call.toString();
    }
}
