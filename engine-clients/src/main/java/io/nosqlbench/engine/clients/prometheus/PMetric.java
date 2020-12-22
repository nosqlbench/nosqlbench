package io.nosqlbench.engine.clients.prometheus;

import java.util.LinkedHashMap;

public class PMetric extends LinkedHashMap<String, String> {
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
