package io.nosqlbench.activitytype.http;

import java.util.regex.Pattern;

public class InvalidStatusCodeException extends RuntimeException {
    private final long cycleValue;
    private final Pattern ok_status;
    private final int statusCode;

    @Override
    public String getMessage() {
        return "Server returned status code '" + statusCode + "' which did not match ok_status '" + ok_status.toString() + "'";
    }

    public InvalidStatusCodeException(long cycleValue, Pattern ok_status, int statusCode) {
        this.cycleValue = cycleValue;
        this.ok_status = ok_status;
        this.statusCode = statusCode;
    }
}
