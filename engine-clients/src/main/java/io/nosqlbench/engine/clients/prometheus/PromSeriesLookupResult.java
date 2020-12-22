package io.nosqlbench.engine.clients.prometheus;

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
