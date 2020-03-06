# cql error handling

The error handling facility utilizes a type-aware error handler
provided by nosqlbench. However, it is much more modular and configurable
than most error handlers found in other testing tools. The trade-off here
is that so many options may bewilder newer users. If you agree, then
simply use one of these basic recipes in your activity parameters:

    # error and stop on *any exception
    # incidentally, this is the same as the deprecated diagnose=true option
    errors=stop
    
    # error and stop for (usually) unrecoverable errors
    # warn and retry everything else (this is actually the default)
    
    errors=stop,retryable->retry

    # record histograms for WriteTimeoutException, error and stop
    # for everything else.
    
    errors=stop,WriteTimeoutException:histogram

As you can see, the error handling format is pretty basic. Behind this basic
format is modular and flexible configuration scheme that should allow for either
simple or advanced testing setups. The errors value is simply a list of error to
hander verbs mappings, but also allows for a simple verb to be specified to
cover all error types. Going from left to right, each mapping is applied in
order. You can use any of ':', '->', or '=' for the error to verb assignment
operator.

Anytime you assign a value to the *errors* parameter for a cql activity, you are
replacing the default 'stop,retryable->retry,unverified->stop' configuration.
That is, each time this value is assigned, a new error handler is configured and
installed according to the new value.
    
### errors= parameter format

The errors parameter contains a comma-separated list of one or more 
handler assignments where the error can be in any of these forms:

- group name ( "unapplied" | "retryable" | "unverified" )
- a single exception name like 'WriteTimeoutException', or a substring of
  that which is long enough to avoid ambiguity (only one match allowed)
- A regex, like '.*WriteTimeout.*' (multiple matches allowed)

The verb can be any of the named starting points in the error handler
stack, as explained below.

As a special case, if the handler assignment consists of only a single word,
then it is assumed to be the default handler verb. This gets applied
as a last resort to any errors which do not match another handler by class
type or parent class type. This allows for simple hard wiring of a
handler default for all non-specific errors in the form:

    # force the test to stop with any error, even retryable ones
    errors=stop
    
### Error Handler Verbs

When an error occurs, you can control how it is handled for the most part.
This is the error handler stack:

- **stop** - logs an error, and then rethrows the causing exception,
    causing nosqlbench to shutdown the current scenario.
- **warn** - log a warning in the log, with details about the error 
    and associated statement.
- **retry** - Retry the operation if the number of retries hasn't been 
    used up *and* the causing exception falls in the set of
    *retryable* errors.
- **histogram** - keep a histogram of the exception counts, under the
    name errorhistos.classname, using the simple class name.
    The magnitude of these histos is how long the operation was pending
    before the related error occurred.
- **count** - keep a count in metrics for the exception, under the name 
    errorcounts.classname, using the simple class name.
- **ignore** - do nothing, do not even retry or count

Each handling verb above is ordered from the most invasive to least 
invasive starting at the top.  With the exception of the **stop** 
handler, the rest of them will be applied to an error all the way 
to the bottom. For now, the error handling stack is exactly as above. 
You can't modify it, although it may be made configurable in the future.
 
One way to choose the right handler is to say "How serious is this type
of error to the test results if it happens?" In general, it is best 
to be more conservative and choose a more aggressive setting unless you 
are specifically wanting to measure how often a given error happens, 
for example.

Each exception type will have one and only one error handler at all times.
No matter how you set an error handler for a class, only the most
recently assigned handler stack will be active for it. This might be
important to keep in mind when you make multiple assignments to potentially
overlapping sets of error types. In any case, the default 'stop' handler
will always catch an error that does not otherwise have a more specific
handler assigned to it. 

##### Error Types

The errors that can be handled are simply all the exception types that
can be thrown by either the DataStax Java Driver for DSE, *or* the
nosqlbench client itself. This includes errors that indicate a potentially
intermittent failure condition. It also includes errors that are more
permanent in nature, like WriteFailure, which would continue to occur
on subsequent retries without some form of intervention. The nosqlbench 
application will also generate some additional exceptions that capture 
common error cases that the Java driver doesn't or shouldn't have a 
special case for, but which may be important for nosqlbench testing purposes.

In nosqlbench, all error handlers are specific to a particular kind of
exception that you would catch in a typical application that uses DSE,
although you can tell a handler to take care of a whole category
of problems as long as you know the right name to use.

##### Assigned by Java Exception Type

Error handlers can be assigned to a common parent type in order to also handle
all known subtypes, hence the default on the top line applies to all of the
driver exceptions that do not have a more specific handler assigned, either
by a closer parent or directly.

##### Assigning by Error Group Name

Error types for which you would commonly assign the same handling behavior
are also grouped in predefined names. If a handler is assigned to one
of the group names, then the handler is assigned all of the exceptions
in the group individually. For example, 'errors=retryable=stop' 

### Recognized Exceptions

The whole hierarchy of exceptions as of DSE Driver 3.2.0 is as follows,
with the default configuration shown. 

    DriverException -> stop
      FrameTooLongException
      CodecNotFoundException
      AuthenticationException
      TraceRetrievalException
      UnsupportedProtocolVersionException
      NoHostAvailableException -> retry (group: retryable)
      QueryValidationException (abstract)
        InvalidQueryException
          InvalidConfigurationInQueryException
        UnauthorizedException
        SyntaxError
        AlreadyExistsException
        UnpreparedException
      InvalidTypeException
      QueryExecutionException (abstract)
        UnavailableException
        BootstrappingException -> retry (group: retryable)
        OverloadedException -> retry (group: retryable)
        TruncateException
        QueryConsistencyException (abstract)
          WriteTimeoutException -> retry (group: retryable)
          WriteFailureException -> retry (group: retryable)
          ReadFailureException
          ReadTimeoutException
        FunctionExecutionException
      DriverInternalError
        ProtocolError
        ServerError
      BusyPoolException
      ConnectionException
        TransportException
        OperationTimedOutException -> retry (group: retryable)
      PagingStateException
      UnresolvedUserTypeException
      UnsupportedFeatureException
      BusyConnectionException
    EbdseException (abstract) -> stop
      CQLResultSetException (abstract)
      UnexpectedPagingException
      ResultSetVerificationException
      RowVerificationException
      ChangeUnappliedCycleException (group:unapplied)
      RetriesExhaustedCycleException -> count

##### Additional Exceptions

The following exceptions are synthesized directly by nosqlbench, but get
handled alongside the normal exceptions as explained above.

1. ChangeUnappliedException - The change unapplied condition is important to
   detect when it is not expected, although some testing may intentionally send
   changes that can't be applied. For this reason, it is kept as a separately
   controllable error group "unapplied".
2. UnexpectedPaging - The UnexpectedPaging exception is meant to keep users from
   being surprised when there is paging activity in the workload, as this can have
   other implications for tuning and performance. See the details on the
   **maxpages** parameter, and the *fetch size* parameter in the java driver for
   details. 
3. Unverified\* Exceptions - For data set verification; These exceptions
   indicate when a cqlverify activity has found rows that differ from what
   was expected.
4. RetriesExhaustedException - Indicates that all retries were exhausted before
   a given operation could complete successfully.
    
