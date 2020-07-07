package io.nosqlbench.nb.api.markdown.types;

import io.nosqlbench.nb.api.markdown.types.HasDiagnostics;

import java.util.List;

public class Diagnostics {
    public static List<String> getDiagnostics(Object o) {
        if (o instanceof HasDiagnostics) {
            return ((HasDiagnostics)o).getDiagnostics();
        }
        return List.of();
    }
    public static List<String> getDiagnostics(Object o, List<String> buffer) {
        if (o instanceof  HasDiagnostics) {
            return ((HasDiagnostics)o).getDiagnostics(buffer);
        }
        return buffer;
    }
}
