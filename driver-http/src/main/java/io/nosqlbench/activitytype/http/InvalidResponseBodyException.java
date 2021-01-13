package io.nosqlbench.activitytype.http;

import java.util.regex.Pattern;

public class InvalidResponseBodyException extends RuntimeException {
    private final long cycleValue;
    private final Pattern ok_body;
    private final String body;

    public InvalidResponseBodyException(long cycleValue, Pattern ok_body, String body) {
        this.cycleValue = cycleValue;
        this.ok_body = ok_body;
        this.body = body;
    }
}
