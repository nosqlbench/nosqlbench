/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.virtdata.lib.vectors.dnn;

import org.junit.jupiter.api.Test;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.core.interfaces.ISymbol;
import org.matheclipse.core.interfaces.IAST;

public class DNN_Symbolic_Tests {

    @Test
    public void testExactRepresentation() {
        ExprEvaluator util = new ExprEvaluator(false, (short)10);

        // Convert an expression to the internal Java form:
        // Note: single character identifiers are case sensitive
        // (the "D()" function identifier must be written as upper case
        // character)
        String javaForm = util.toJavaForm("D(sin(x)*cos(x),x)");
        // prints: D(Times(Sin(x),Cos(x)),x)
        System.out.println("Out[1]: " + javaForm.toString());

        // Use the Java form to create an expression with F.* static
        // methods:
        ISymbol x = F.Dummy("x");
        IAST function = F.D(F.Times(F.Sin(x), F.Cos(x)), x);
        IExpr result = util.eval(function);
        // print: Cos(x)^2-Sin(x)^2
        System.out.println("Out[2]: " + result.toString());

        // Note "diff" is an alias for the "D" function
        result = util.eval("diff(sin(x)*cos(x),x)");
        // print: Cos(x)^2-Sin(x)^2
        System.out.println("Out[3]: " + result.toString());

        // evaluate the last result (% contains "last answer")
        result = util.eval("%+cos(x)^2");
        // print: 2*Cos(x)^2-Sin(x)^2
        System.out.println("Out[4]: " + result.toString());

        // evaluate an Integrate[] expression
        result = util.eval("integrate(sin(x)^5,x)");
        // print: 2/3*Cos(x)^3-1/5*Cos(x)^5-Cos(x)
        System.out.println("Out[5]: " + result.toString());

        // set the value of a variable "a" to 10
        result = util.eval("a=10");
        // print: 10
        System.out.println("Out[6]: " + result.toString());

        // do a calculation with variable "a"
        result = util.eval("a*3+b");
        // print: 30+b
        System.out.println("Out[7]: " + result.toString());

        // Do a calculation in "numeric mode" with the N() function
        // Note: single character identifiers are case sensistive
        // (the "N()" function identifier must be written as upper case
        // character)
        result = util.eval("N(sinh(5))");
        // print: 74.20321057778875
        System.out.println("Out[8]: " + result.toString());

        // define a function with a recursive factorial function definition.
        // Note: fac(0) is the stop condition.
        result = util.eval("fac(x_Integer):=x*fac(x-1);fac(0)=1");
        // now calculate factorial of 10:
        result = util.eval("fac(10)");
        // print: 3628800
        System.out.println("Out[9]: " + result.toString());

        function = F.Function(F.Divide(F.Gamma(F.Plus(F.C1, F.Slot1)), F.Gamma(F.Plus(F.C1, F.Slot2))));
        // eval function ( Gamma(1+#1)/Gamma(1+#2) ) & [23,20]
        result = util.evalFunction(function, "23", "20");
        // print: 10626
        System.out.println("Out[10]: " + result.toString());
    }
}
