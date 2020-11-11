package com.datastax.ebdrivers.dsegraph.errorhandling;

/**
 * When an error filter allows us to see and handle an error in a specific way,
 * the ErrorResponse determine exactly how we handle it. Each level represents
 * a starting point in handling, including everything after the starting point.
 * The first enum is the most severe response.
 */
public enum ErrorResponse {
    stop,      // Rethrow this error to the runtime, forcing it to handle the error or stop
    warn,      // log a warning with some details about this error
    retry,     // resubmit this operation up to the available tries
    count,     // count this metric separatelycount,     // count this metric separately
    ignore     // do nothing
}
