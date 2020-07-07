package io.nosqlbench.nb.api.markdown.types;

import java.util.List;

public interface HasDiagnostics {
    List<String> getDiagnostics(List<String> buffer);
    List<String> getDiagnostics();
}
