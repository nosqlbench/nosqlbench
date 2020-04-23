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

package io.nosqlbench.engine.extensions.optimizers;

import com.codahale.metrics.MetricRegistry;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.slf4j.Logger;

import javax.script.ScriptContext;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class BobyqaOptimizerInstance {

    private final Logger logger;
    private final MetricRegistry metricRegistry;
    private final ScriptContext scriptContext;

    private int interpolations = 0;
    private double initialTrustRegionRadius = Double.MAX_VALUE;
    private double stoppingTrustRegionRadius = 1.0D;

    private MVParams params = new MVParams();

    private MultivariateFunction objectiveFunctionFromScript;
    private SimpleBounds bounds;
    private InitialGuess initialGuess;
    private PointValuePair result;
    private int maxEval;
    private MVLogger mvLogger;
    private double guessSlew = 0.25d;

    public BobyqaOptimizerInstance(Logger logger, MetricRegistry metricRegistry, ScriptContext scriptContext) {
        this.logger = logger;
        this.metricRegistry = metricRegistry;
        this.scriptContext = scriptContext;
    }

    public BobyqaOptimizerInstance setPoints(int numberOfInterpolationPoints) {
        this.interpolations = numberOfInterpolationPoints;
        return this;
    }

    public BobyqaOptimizerInstance setInitialRadius(double initialTrustRegionRadius) {
        this.initialTrustRegionRadius = initialTrustRegionRadius;
        return this;
    }

    public BobyqaOptimizerInstance setStoppingRadius(double stoppingTrustRegionRadius) {
        this.stoppingTrustRegionRadius = stoppingTrustRegionRadius;
        return this;
    }

    public BobyqaOptimizerInstance setMaxPoints() {
        return this.setPoints(getMaxInterpolations());
    }

    public int getMaxInterpolations() {
        return (int) (0.5d * ((this.params.size() + 1) * (this.params.size() + 2)));
    }

    public BobyqaOptimizerInstance setMinPoints() {
        return this.setPoints(getMinInterpolations());
    }

    public int getMinInterpolations() {
        return this.params.size() + 2;
    }

    public BobyqaOptimizerInstance setBounds(double... values) {
        double[] bottom = Arrays.copyOfRange(values, 0, values.length >> 1);
        double[] top = Arrays.copyOfRange(values, values.length >> 1, values.length);
        this.bounds = new SimpleBounds(bottom, top);
        return this;
    }

    public BobyqaOptimizerInstance setObjectiveFunction(Object f) {
        if (f instanceof ScriptObjectMirror) {
            ScriptObjectMirror scriptObject = (ScriptObjectMirror) f;
            if (!scriptObject.isFunction()) {
                throw new RuntimeException("Unable to setFunction with a non-function object");
            }
            this.objectiveFunctionFromScript =
                    new NashornMultivariateObjectScript(logger, params, scriptObject);
        }

        if (f instanceof Function) {
//            Function<Object[],Object> function = (Function<Object[],Object>)f;
            this.objectiveFunctionFromScript =
                new PolyglotMultivariateObjectScript(logger, params, f);
        }

        return this;
    }

    public BobyqaOptimizerInstance setMaxEval(int maxEval) {
        this.maxEval = maxEval;
        return this;
    }

    public MVResult optimize() {
        initialGuess = initialGuess == null ? computeInitialGuess() : initialGuess;
        bounds = bounds == null ? computeBounds() : bounds;
        interpolations = interpolations == 0 ? getMinInterpolations() : interpolations;

        BOBYQAOptimizer mo = new BOBYQAOptimizer(
                this.interpolations,
                this.initialTrustRegionRadius,
                this.stoppingTrustRegionRadius
        );

        this.mvLogger = new MVLogger(this.objectiveFunctionFromScript);
        ObjectiveFunction objective = new ObjectiveFunction(this.mvLogger);

        List<OptimizationData> od = List.of(
                objective,
                GoalType.MAXIMIZE,
                this.initialGuess,
                new MaxEval(this.maxEval),
                this.bounds
        );

        this.result = mo.optimize(od.toArray(new OptimizationData[0]));

        return new MVResult(
                this.result.getPoint(),
                this.params,
                this.mvLogger.getLogArray()
        );
    }

    public BobyqaOptimizerInstance setGuessRatio(double slew) {
        this.guessSlew = slew;
        return this;
    }

    private SimpleBounds computeBounds() {
        double[] lb = new double[params.size()];
        double[] ub = new double[params.size()];
        int pos = 0;
        for (MVParams.MVParam param : params) {
            lb[pos] = param.min;
            ub[pos] = param.max;
            pos++;
        }
        return new SimpleBounds(lb, ub);
    }

    private InitialGuess computeInitialGuess() {
        double[] guess = new double[params.size()];
        int pos = 0;
        for (MVParams.MVParam param : params) {
            guess[pos++] = param.min + ((param.max - param.min)*guessSlew);
        }
        return new InitialGuess(guess);
    }

    public BobyqaOptimizerInstance param(String name, double min, double max) {
        params.addParam(name, min, max);
        return this;
    }

    public double[] getResult() {
        return result.getPoint();
    }
}
