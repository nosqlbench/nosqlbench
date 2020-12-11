package io.nosqlbench.nb.api.labels;

import java.util.Map;

public interface Labeled {
    Map<String, String> getLabels();
}
