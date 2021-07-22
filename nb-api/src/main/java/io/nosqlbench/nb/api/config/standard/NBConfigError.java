package io.nosqlbench.nb.api.config.standard;

import io.nosqlbench.nb.api.errors.BasicError;

public class NBConfigError extends BasicError {
    public NBConfigError(String s) {
        super(s);
    }
}
