DriverException -> stop
  1   FrameTooLongException
  2   CodecNotFoundException
  3   AuthenticationException
  4   TraceRetrievalException
  5   UnsupportedProtocolVersionException
  6   NoHostAvailableException
  7   QueryValidationException (abstract)
  8     InvalidQueryException
  9       InvalidConfigurationInQueryException
  10    UnauthorizedException
  11    SyntaxError
  12    AlreadyExistsException
  13    UnpreparedException
  14  InvalidTypeException
  15  QueryExecutionException (abstract) -> retry
  16    UnavailableException
  17    BootstrappingException
  18    OverloadedException
  19    TruncateException
  20    QueryConsistencyException (abstract)
  21      WriteTimeoutException
  22      WriteFailureException
  23      ReadFailureException
  24      ReadTimeoutException
  25    FunctionExecutionException
  26  DriverInternalError
  27    ProtocolError
  28    ServerError
  29  BusyPoolException
  30  ConnectionException
  31    TransportException
  32    OperationTimedOutException
  33  PagingStateException
  34  UnresolvedUserTypeException
  35  UnsupportedFeatureException
  36  BusyConnectionException
  41  EbdseCycleException (abstract) -> stop
  37    ChangeUnappliedCycleException
  38    ResultSetVerificationException
  39    RowVerificationException (abstract)
  40    UnexpectedPagingException
