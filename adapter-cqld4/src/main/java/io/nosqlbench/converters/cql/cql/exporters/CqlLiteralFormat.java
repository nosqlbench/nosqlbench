package io.nosqlbench.converters.cql.cql.exporters;

import java.util.function.Function;

public enum CqlLiteralFormat {
    text(v -> "\""+v+"\""),
    UNKNOWN(v -> v);

    private final Function<String, String> literalFormat;
    CqlLiteralFormat(Function<String,String> modifier) {
        this.literalFormat = modifier;
    }

    public String format(String value) {
        return this.literalFormat.apply(value);
    }
}
