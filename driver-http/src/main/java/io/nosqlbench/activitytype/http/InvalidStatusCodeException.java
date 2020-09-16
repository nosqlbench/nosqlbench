package io.nosqlbench.activitytype.http;

public class InvalidStatusCodeException extends RuntimeException {
    private final long cycleValue;
    private final String ok_status;
    private final int statusCode;

    public InvalidStatusCodeException(long cycleValue, String ok_status, int statusCode) {
        this.cycleValue = cycleValue;
        this.ok_status = ok_status;
        this.statusCode = statusCode;
    }
}
