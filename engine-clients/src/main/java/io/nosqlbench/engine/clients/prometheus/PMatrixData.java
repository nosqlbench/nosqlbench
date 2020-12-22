package io.nosqlbench.engine.clients.prometheus;

import java.util.List;

public class PMatrixData {
    String resultType;
    List<PMatrixElem> result;

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public List<PMatrixElem> getResult() {
        return result;
    }

    public void setResult(List<PMatrixElem> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "PMatrixData{" +
                "resultType='" + resultType + '\'' +
                ", result=" + result +
                '}';
    }
}
