# NBErrorHandler API

NoSQLBench provides a standard way of configuring error handling across
driver implementations. This is provided by NBErrorHandler and associated
classes.

This error handler allows a chain of specific error handler behaviors to
be invoked based on which type of error occurs. Additionally, the error
handler is responsible for determining the cycle code to be used in
advanced testing as well as for indicating whether or not an operation is
eligible to be retried or not.

The entire implementation is of this error handler is in package:

    io.nosqlbench.engine.api.activityapi.errorhandling.modular

The canonical example of using this API can be found in HttpAction.

## NBErrorHandler Sketch

As a feature that changes control-flow based on user input, it strongly
suggests a specific type of flow within an action. The following
pseudo-code explains this pattern.

1. When an activity is initialized, also initialize an instance of
   NBErrorHandler
2. Each time an Action is called, loop while retries are not exhausted.
    1. catch all exceptions and pass them to the error handler
    2. Read the returned ErrorDetail.
    3. If the error detail indicates that the error was retryable and
       max_tries is not exhausted, retry the operation Otherwise, stop the
       cycle.
    4. In any case, when the cycle is complete, always return the cycle
       code in the error detail, or 0 if no error was found

