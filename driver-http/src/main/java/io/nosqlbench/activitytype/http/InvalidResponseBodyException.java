package io.nosqlbench.activitytype.http;

public class InvalidResponseBodyException extends RuntimeException {
    private final long cycleValue;
    private final String ok_body;
    private final String body;

    public InvalidResponseBodyException(long cycleValue, String ok_body, String body) {
        this.cycleValue = cycleValue;
        this.ok_body = ok_body;
        this.body = body;
    }
}
