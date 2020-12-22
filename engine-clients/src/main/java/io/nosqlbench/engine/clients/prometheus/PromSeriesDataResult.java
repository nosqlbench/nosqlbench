package io.nosqlbench.engine.clients.prometheus;

import java.util.List;
import java.util.Map;

public class PromSeriesDataResult {
    String status;
    Data data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        String resultType;
        List<Result> result;

        public String getResultType() {
            return resultType;
        }

        public void setResultType(String resultType) {
            this.resultType = resultType;
        }

        public List<Result> getResult() {
            return result;
        }

        public void setResult(List<Result> result) {
            this.result = result;
        }

        private static class Result {
            Map<String, String> metric;
            Value[] values;

            public Map<String, String> getMetric() {
                return metric;
            }

            public void setMetric(Map<String, String> metric) {
                this.metric = metric;
            }

            public Value[] getValues() {
                return values;
            }

            public void setValues(Value[] values) {
                this.values = values;
            }

            public static class Value {
                long instant;
                String value;

                public long getInstant() {
                    return instant;
                }

                public void setInstant(long instant) {
                    this.instant = instant;
                }

                public String getValue() {
                    return value;
                }

                public void setValue(String value) {
                    this.value = value;
                }
            }
        }
    }
}
