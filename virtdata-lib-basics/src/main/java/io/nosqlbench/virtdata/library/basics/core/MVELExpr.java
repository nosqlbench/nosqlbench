package io.nosqlbench.virtdata.library.basics.core;

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
