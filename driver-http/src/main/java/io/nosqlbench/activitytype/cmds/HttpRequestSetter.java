package io.nosqlbench.activitytype.cmds;

import io.nosqlbench.engine.api.templating.EnumSetter;

import java.net.http.HttpRequest;

public class HttpRequestSetter implements EnumSetter<ReadyHttpRequest.FieldType,HttpRequest.Builder> {

    @Override
    public HttpRequest.Builder setField(
            HttpRequest.Builder target,
            ReadyHttpRequest.FieldType field,
            Object... value) {
        switch (field) {
            case method:
                return target.method((String)value[0], (HttpRequest.BodyPublisher) value[1]);
            case host:
            case path:
            case query:
            case header:
            case version:
                return target;
            default:
                throw new RuntimeException("field type was not set correctly:" + field);
        }

    }


}
