package io.nosqlbench.core;

/**
 * User exceptions are errors for which we know how to explain the cause to the user.
 * For these, we should not need to log or report stack traces to any channel, as
 * the cause of and thus the remedy for the error should be very obvious.
 */
public class UserException extends RuntimeException {
    public UserException(String exception) {
        super(exception);
    }
}
