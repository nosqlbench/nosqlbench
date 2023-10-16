package io.nosqlbench.adapter.http.core;

import com.google.gson.JsonElement;

public enum HttpResultType {
    none(Void.class),
    string(String.class),
    json_element(JsonElement.class);

    public  final Class<?> resultClass;

    HttpResultType(Class<?> resultClass) {
        this.resultClass = resultClass;
    }
}
