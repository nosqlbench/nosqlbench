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


import java.util.ArrayList;
import java.util.List;

public class VirtDataFlow {

    private final List<Expression> expressions = new ArrayList<>();

    public List<Expression> getExpressions() {
        return expressions;
    }

    public void addExpression(Expression expr) {
        expressions.add(expr);
    }

    public Expression getLastExpression() {
        if (expressions.size()==0) {
            throw new RuntimeException("expressions not initialized, last expression undefined");
        }
        return expressions.get(expressions.size()-1);
    }

    public Expression getFirstExpression() {
        if (expressions.size()==0) {
            throw new RuntimeException("expressions not initialized, first expression undefined.");
        }
        return expressions.get(0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Expression expression : expressions) {
            sb.append(expression).append("; ");
        }
        if (sb.length()>0) {
            sb.setLength(sb.length()-"; ".length());
        }
        return sb.toString();
    }
}
