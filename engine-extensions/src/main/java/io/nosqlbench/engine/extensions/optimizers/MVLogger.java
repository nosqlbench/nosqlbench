package io.nosqlbench.engine.extensions.optimizers;

import org.apache.commons.math3.analysis.MultivariateFunction;

import java.util.ArrayList;
import java.util.List;

public class MVLogger implements MultivariateFunction {
    private final MultivariateFunction function;
    List<List<Double>> log = new ArrayList<>();

    public MVLogger(MultivariateFunction function) {
        this.function = function;
    }

    @Override
    public double value(double[] doubles) {
        ArrayList<Double> params = new ArrayList<>(doubles.length);
        log.add(params);

        return function.value(doubles);
    }

    public List<List<Double>> getLogList() {
        return log;
    }

    public double[][] getLogArray() {
        double[][] ary = new double[log.size()][];
        for (int row = 0; row < log.size(); row++) {
            List<Double> columns = log.get(row);
            double[] rowary = new double[columns.size()];
            ary[row]=rowary;
            for (int col = 0; col < log.get(row).size(); col++) {
                rowary[col]=columns.get(col);
            }
        }
        return ary;
    }
}
