package io.nosqlbench.optimizers;
/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * Consider JSAT
 *
 * https://en.wikipedia.org/wiki/Coordinate_descent
 *
 * http://firsttimeprogrammer.blogspot.com/2014/09/multivariable-gradient-descent.html
 *
 * https://towardsdatascience.com/machine-learning-bit-by-bit-multivariate-gradient-descent-e198fdd0df85
 *
 * https://pdfs.semanticscholar.org/d142/3994d7b4462994925663959721130755b275.pdf
 *
 * file:///tmp/bfgs-example.pdf
 *
 * https://github.com/vinhkhuc/lbfgs4j
 *
 * https://github.com/EdwardRaff/JSAT/wiki/Algorithms
 *
 * http://www.optimization-online.org/DB_FILE/2010/05/2616.pdf
 *
 * https://github.com/dpressel/sgdtk
 *
 */
public class TestOptimoExperiments {

    @Test
    public void testNewAlgo() {

        MultivariateFunction m = new SumDeltaNoise();

//        MultivariateDifferentiableFunction mvdf =FunctionUtils.
//        MultivariateVectorFunction mvf = new GradientFunction(mvdf);
//        ObjectiveFunctionGradient ofg = new ObjectiveFunctionGradient(mvf);


        SimpleBounds bounds = new SimpleBounds(
                new double[]{0.0d, 0.0d, 0.0d},
                new double[]{1E9,1E9,1E9});

        List<OptimizationData> od = List.of(
                new ObjectiveFunction(m),
                GoalType.MAXIMIZE,
                new InitialGuess(new double[]{1.0,1.0,1.0}),
                new MaxEval(1000)
                ,bounds
        );

        BOBYQAOptimizer mo = new BOBYQAOptimizer(
                9,
                1000.0,
                1.0
        );
        PointValuePair result = mo.optimize(od.toArray(new OptimizationData[0]));

        System.out.println(
                "point:" + Arrays.toString(result.getPoint()) +
                        " value=" + m.value(result.getPoint())
        );

    }

    private static class SumDeltaNoise implements MultivariateFunction {
        private int iter;
        private Random r = new Random(System.nanoTime());

        @Override
        public double value(double[] doubles) {
            iter++;

            double value = r.nextDouble()*10.0;
            System.out.format("i:%d NOISE=%.3f GUESS=%s\n",iter, value, Arrays.toString(doubles));

            double product = 1.0d;
            for (double aDouble : doubles) {
                double component = 100.0 - Math.abs(aDouble-100);
                product+=component;
                System.out.print(" +" + component);
            }
            value += product;
            System.out.format(" val=%.3f\n\n",value);
            return value;
        }
    }

//    private final static DoubleBinaryOperator f1 = (a,b) -> {
//        return 5+Math.pow(a,2.0d)+Math.pow(b,2.0d);
//    };

}