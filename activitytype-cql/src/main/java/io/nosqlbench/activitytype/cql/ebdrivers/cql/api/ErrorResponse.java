package io.nosqlbench.activitytype.cql.ebdrivers.cql.api;

/**
 * When an error filter allows us to see and handle an error in a specific way,
 * the ErrorResponse determines exactly how we handle it. Each level represents
 * a starting point in handling, including everything after the starting point.
 * The first enum is the most severe response
 */
public enum ErrorResponse {

    stop("S"),      // Rethrow this error to the runtime, forcing it to handle the error or stop
    warn("W"),      // log a warning with some details about this error
    retry("R"),     // resubmit this operation up to the available tries
    histogram("H"),     // record this metric in a histogram
    count("C"),     // count this metric separately
    ignore("I");    // do nothing

    private String symbol;

    ErrorResponse(String symbol) {
        this.symbol = symbol;
    }
}
