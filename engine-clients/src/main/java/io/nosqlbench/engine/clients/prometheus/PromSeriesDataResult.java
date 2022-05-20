package io.nosqlbench.engine.clients.prometheus;

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
