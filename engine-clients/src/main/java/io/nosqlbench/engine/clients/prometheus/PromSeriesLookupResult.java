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


import java.util.LinkedHashMap;
import java.util.List;

public class PromSeriesLookupResult {
    String status;
    List<Element> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Element> getData() {
        return data;
    }

    public void setData(List<Element> data) {
        this.data = data;
    }

    public static class Element extends LinkedHashMap<String, String> {

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            forEach((k, v) -> {
                sb.append(k).append("=");
                sb.append("\"").append(v).append("\",");
            });
            sb.setLength(sb.length() - 1);
            sb.append("}");
            return sb.toString();
        }
    }
}
