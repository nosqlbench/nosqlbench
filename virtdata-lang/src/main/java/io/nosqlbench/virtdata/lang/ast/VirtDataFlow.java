package io.nosqlbench.virtdata.lang.ast;

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
