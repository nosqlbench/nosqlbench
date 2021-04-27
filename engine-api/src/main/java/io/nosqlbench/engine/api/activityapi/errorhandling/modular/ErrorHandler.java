package io.nosqlbench.engine.api.activityapi.errorhandling.modular;

public interface ErrorHandler {
    /**
     * An error handler is able to do side-effect processing for a particular
     * error type, as well as modify the error detail that will be presented
     * back to the caller.
     *
     * @param t               Thrown from within the activity's action loop
     * @param cycle           The cycle which was executing when it ran
     * @param durationInNanos How long into an operation the error occured
     * @return true if the operation should be retried, assuming retries available
     */
    ErrorDetail handleError(String name, Throwable t, long cycle, long durationInNanos, ErrorDetail detail);
}
