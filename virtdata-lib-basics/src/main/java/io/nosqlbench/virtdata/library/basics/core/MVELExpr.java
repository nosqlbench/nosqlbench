package io.nosqlbench.virtdata.library.basics.core;

import org.mvel2.MVEL;
import org.mvel2.ParserContext;

import java.io.Serializable;

/**
 * Utility class for creating pre-compiled MVEL expressions with a typed and named context variable.
 */
public class MVELExpr {

    public static Serializable compile(Class<?> inputClass, String inputName, String expr) {
        ParserContext context = new ParserContext();
        context.setStrictTypeEnforcement(true);
        context.addInput(inputName,inputClass);
        Serializable compiled = MVEL.compileExpression(expr, context);
        return compiled;
    }
}
