package io.nosqlbench.nb.api.errors;

/**
 * ActivityInitErrors are those known to occur during the initiailization of an activity.
 */
public class ActivityInitError extends RuntimeException {
    public ActivityInitError(String message, Throwable cause) {
        super(message, cause);
    }

    public ActivityInitError(String error) {
        super(error);
    }
}
