package io.nosqlbench.extensions.optimizers;

import java.util.HashMap;
import java.util.Map;

public class MVResult {

    private final double[] vars;
    private final MVParams params;

    public MVResult(double[] vars, MVParams params, double[][] datalog) {
        this.params = params;
        this.vars = vars;
    }

    public double[] getVarArray() {
        return vars;
    }

    public Map<String,Double> getVarMap() {
        Map<String,Double> result = new HashMap<>(params.size());
        for (int i = 0; i < vars.length; i++) {
            result.put(params.get(i).name,vars[i]);
        }
        return result;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        int pos=0;
        for (MVParams.MVParam param : params) {
            sb.append(param.name).append("=").append(vars[pos])
                    .append(" [").append(param.min).append("..").append(param.max)
                    .append("]");
            sb.append("\n");
            pos++;
        }
        sb.setLength(sb.length()-1);

        return sb.toString();
    }
}
