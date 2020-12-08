# cql driver

This is a driver which allows for the execution of CQL statements. This driver supports both sync and async modes, with
detailed metrics provided for both.

### Example activity definitions

Run a cql activity named 'cql1', with definitions from activities/cqldefs.yaml

    ... driver=cql alias=cql1 workload=cqldefs

Run a cql activity defined by cqldefs.yaml, but with shortcut naming

    ... driver=cql workload=cqldefs

Only run statement groups which match a tag regex

    ... driver=cql workload=cqldefs tags=group:'ddl.*'

Run the matching 'dml' statements, with 100 cycles, from [1000..1100)

    ... driver=cql workload=cqldefs tags=group:'dml.*' cycles=1000..1100

This last example shows that the cycle range is [inclusive..exclusive),
to allow for stacking test intervals. This is standard across all
activity types.

### CQL ActivityType Parameters

- **cqldriver** - default: dse - The type of driver to use, either dse, or oss. If you need DSE-specific features, use
  the dse driver. If you are connecting to an OSS Apache Cassandra cluster, you must use the oss driver. The oss driver
  option is only available in nosqlbench.
- **host** - The host or hosts to use for connection points to
    the cluster. If you specify multiple values here, use commas
    with no spaces.
    Examples:
    - `host=192.168.1.25`
    - `host=192.168.1.25,testhost42`
- **workload** - The workload definition which holds the schema and statement defs.
     see workload yaml location for additional details
    (no default, required)
- **port** - The port to connect with
- **cl** - An override to consistency levels for the activity. If
    this option is used, then all consistency levels will be replaced
    by this one for the current activity, and a log line explaining
    the difference with respect to the yaml will be emitted.
    This is not a dynamic parameter. It will only be applied at
    activity start.
- **cbopts** - default: none - this is how you customize the cluster
    settings for the client, including policies, compression, etc. This
    is a string of *Java*-like method calls just as you would use them
    in the Cluster.Builder fluent API. They are evaluated inline
    with the default Cluster.Builder options not covered below.
    Example: cbopts=".withCompression(ProtocolOptions.Compression.NONE)"
- **whitelist** default: none - Applies a whitelist policy to the load balancing
    policy in the driver. If used, a WhitelistPolicy(RoundRobinPolicy())
    will be created and added to the cluster builder on startup.
    Examples:
    - `whitelist=127.0.0.1`
    - `whitelist=127.0.0.1:9042,127.0.0.2:1234`
- **retrypolicy** default: none - Applies a retry policy in the driver
    The only option supported for this version is `retrypolicy=logging`,
    which uses the default retry policy, but with logging added.

- **reconnectpolicy** default: none - Applies a reconnection policy in the driver
    Supports either `reconnectpolicy=exponential(minDelayInMs,maxDelayInMs)` or `reconnectpolicy=constant(delayInMs)`.
    The driver reconnects using this policy when the entire cluster becomes unavailable.


- **pooling** default: none - Applies the connection pooling options
    to the policy.
    Examples:
    - `pooling=4:10`
      keep between 4 and 10 connections to LOCAL hosts
    - `pooling=4:10,2:5`
      keep 4-10 connections to LOCAL hosts and 2-5 to REMOTE
    - `pooling=4:10:2000`
      keep between 4-10 connections to LOCAL hosts with
      up to 2000 requests per connection
  -   `pooling=5:10:2000,2:4:1000` keep between 5-10 connections to
      LOCAL hosts with up to 2000 requests per connection, and 2-4
      connection to REMOTE hosts with up to 1000 requests per connection

    Additionally, you may provide the following options on pooling. Any
    of these that are provided must appear in this order:
    `,heartbeat_interval_s:n,idle_timeout_s:n,pool_timeout_ms:n`, so a
    full example with all options set would appear as:
    `pooling=5:10:2000,2:4:1000,heartbeat_interval_s:30,idle_timeout_s:120,pool_timeout_ms:5`

- **socketoptions** default: none - Applies any of the valid socket
  options to the client when the session is built. Each of the options
  uses the long form of the name, with either a numeric or boolean
  value. Individual sub-parameters should be separated by a comma, and
  the parameter names and values can be separated by either equals or a
  colon. All of these values may be changed:
  - read_timeout_ms
  - connect_timeout_ms
  - keep_alive
  - reuse_address
  - so_linger
  - tcp_no_delay
  - receive_buffer_size
  - send_buffer_size

  Examples:
    - `socketoptions=read_timeout_ms=23423,connect_timeout_ms=4444`
    - `socketoptions=tcp_no_delay=true`

- **tokens** default: unset - Only executes statements that fall within
    any of the specified token ranges. Others are counted in metrics
    as skipped-tokens, with a histogram value of the cycle number.
    Examples:
    - tokens=1:10000,100000:1000000
    - tokens=1:123456
- **maxtries** - default: 10 - how many times an operation may be
    attempted before it is disregarded
- **maxpages** - default: 1 - how many pages can be read from a query which
    is larger than the fetchsize. If more than this number of pages
    is required for such a query, then an UnexpectedPaging excpetion
    is passed to the error handler as explained below.
- **fetchsize** - controls the driver parameter of the same name.
    Suffixed units can be used here, such as "50K". If this parameter
    is not present, then the driver option is not set.
- **cycles** - standard, however the cql activity type will default
    this to however many statements are included in the current
    activity, after tag filtering, etc.
- **username** - the user to authenticate as. This option requires
    that one of **password** or **passfile** also be defined.
- **password** - the password to authenticate with. This will be
    ignored if passfile is also present.
- **passfile** - the file to read the password from. The first
    line of this file is used as the password.

- **ssl** - specifies the type of the SSL implementation.
    Disabled by default, possible values are `jdk` and `openssl`.

    [Additional parameters may need to be provided](ssl.md).

- **jmxreporting** - enable JMX reporting if needed.
    Examples:
    - `jmxreporting=true`
    - `jmxreporting=false` (the default)
- **alias** - this is a standard nosqlbench parameter, however the cql type will use the workload value also as the
  alias value when not specified.
- **errors** - error handler configuration.
    (default errors=stop,retryable->retry,unverified->stop)
    Examples:
    - errors=stop,WriteTimeoutException=histogram
    - errors=count
    - errors=warn,retryable=count
    See the separate help on 'cqlerrors' for detailed
    configuration options.
- **defaultidempotence** - sets default idempotence on the
    driver options, but only if it has a value.
    (default unset, valid values: true or false)
- **speculative** - sets the speculative retry policy on the cluster.
    (default unset)
    This can be in one of the following forms:
    - pT:E:L - where :L is optional and
      T is a floating point threshold between 0.0 and 100.0 and
      E is an allowed number of concurrent speculative executions and
      L is the maximum latency tracked in the tracker instance
      (L defaults to 15000 when left out)
      Examples:
      - p99.8:5:15000ms - 99.8 percentile, 5 executions, 15000ms max tracked
      - p98:2:10000ms - 98.0 percentile, 2 executions allowed, 10s max tracked
    - Tms:E - where :E is optional and
      T is a constant threshold latency and
      E is the allowed number of concurrent speculative retries
      (E default to 5 when left out)
      Examples:
      - 100ms:5 - constant threshold of 100ms and 5 allowed executions
- **seq** - selects the statement sequencer used with statement ratios.
    (default: bucket)
    (options: concat | bucket | interval)
    The concat sequencer repeats each statement in order until the ratio
    is achieved.
    The bucket sequencer uses simple round-robin distribution to plan
    statement ratios, a simple but unbalanced form of interleaving.
    The interval sequencer apportions statements over time and then by
    order of appearance for ties. This has the effect of interleaving
    statements from an activity more evenly, but is less obvious in how
    it works.
    All of the sequencers create deterministic schedules which use an internal
    lookup table for indexing into a list of possible statements.
- **trace** - enables a trace on a subset of operations. This is disabled
    by default.
    Examples:
    `trace=modulo:100,filename:trace.log`
    The above traces every 100th cycle to a file named trace.log.
    `trace=modulo:1000,filename:stdout`
    The above traces every 1000th cycle to stdout.
    If the trace log is not specified, then 'tracelog' is assumed.
    If the filename is specified as stdout, then traces are dumped to stdout.
- **sessionid** - names the configuration to be used for this activity. Within a given scenario, any activities that use
  the same name for clusterid will share a session and cluster. default: 'default'
- **drivermetrics** - enable reporting of driver metrics.
    default: false
- **driverprefix** - set the metrics name that will prefix all CQL driver metrics.
    default: 'driver.*clusterid*.'
    The clusterid specified is included so that separate cluster and session
    contexts can be reported independently for advanced tests.
- **usercodecs** - enable the loading of user codec libraries
    for more details see: com.datastax.codecs.framework.UDTCodecInjector in the nosqlbench
    code base. This is for dynamic codec loading with user-provided codecs mapped
    via the internal UDT APIs.
    default: false
- **secureconnectbundle** - used to connect to CaaS, accepts a path to the secure connect bundle
    that is downloaded from the CaaS UI.
    Examples:
    - `secureconnectbundle=/tmp/secure-connect-my_db.zip`
    - `secureconnectbundle="/home/automaton/secure-connect-my_db.zip"`
    Astra Example:
    - `$ nb run driver=cql \
    workload=cql-iot \
    tags=phase:schema \
    username=astra_user \
    password=astra_password \
    secureconnectbundle=/path/to/secure-connect-mydatabase.zip`
- **insights** - Set to false to disable the driver from sending insights monitoring information
    - `insights=false`
- **tickduration** - sets the tickDuration (milliseconds) of HashedWheelTimer of the
    java driver. This timer is used to schedule speculative requests.
    Examples:
    - `tickduration=10`
    - `tickduration=100` (driver default value)
- **compression** - sets the transport compression to use for this
    activity. Valid values are 'LZ4' and 'SNAPPY'. Both types are bundled
    with EBDSE.
- **showcql** - logs cql statements as INFO (to see INFO messages in stdout use -v or greater) Note: this is expensive
    and should only be done to troubleshoot workloads. Do not use `showcql` for your tests.
- **lbp** - configures the load balancing policies for the Java driver. With this parameter, you can
  configure nested load balancing policies in short-hand form.

  The policies available are documented in detail under the help topic `cql-loadbalancing`. See that
  guide if you need more than the examples below.

  Examples:
  - `lbp=LAP(retry_period=3,scale=10)` - Latency aware policy with retry period of 3 seconds.
    (Seconds is the default time unit, unless _ms parameter is used) and scale 10.
  - `lbp=LAP(rp=3,s=10)` - Same as above, using the equivalent but terser form.
  - `lbp=LAP(rp_ms=3000,s_ms=10000)` - Same as above, with milliseconds instead of
    seconds.
  - `loadbalancing=LAP(s=10),TAP()` - Latency aware policy, followed by
    token aware policy.

### CQL YAML Parameters

A uniform YAML configuration format was introduced with engineblock 2.0.
As part of this format, statement parameters were added for the CQL Activity Type.
These parameters will be consolidated with the above parameters in time, but for
now **they are limited to a YAML params block**:

    params:

     ratio: 1
     # Sets the statement ratio within the operation sequencer
     # scheme. Integers only.
     # When preparing the operation order (AKA sequencing),
     # frequency of the associated statements.

     cl: ONE
     # Sets the consistency level, using any of the standard
     # identifiers from com.datastax.driver.core.ConsistencyLevel,
     # any one of:
     # LOCAL_QUORUM, ANY, ONE, TWO, THREE, QUORUM, ALL,
     # EACH_QUORUM, SERIAL, LOCAL_SERIAL, LOCAL_ONE

     prepared: true
     # By default, all statements are prepared. If you are
     # creating schema, set this to false.

     idempotent: false
     # For statements that are known to be idempotent, set this
     # to true

     instrument: false
     # If a statement has instrument set to true, then
     # individual Timer metrics will be tracked for
     # that statement for both successes and errors,
     # using the given statement name.

    logresultcsv: true
    OR
    logresultcsv: myfilename.csv
    # If a statement has logresultcsv set to true,
    # then individual operations will be logged to a CSV file.
    # In this case the CSV file will be named as
    # <statement-name>--results.csv.
    # If the value is present and not "true", then the value will
    # be used as the name of the file.
    #
    # The format of the file is:
    # <cycle>,(SUCCESS|FAILURE),<nanos>,<rows-fetched>,(<error-class,NONE)
    # NOTES:
    # 1) BE CAREFUL with this setting. A single logged line per
    #    result is not useful for high-speed testing as it will
    #    impose IO loads on the client to slow it down.
    # 2) BE CAREFUL with the name. It is best to just pick good
    #    names for your statement defs so that everything remains
    #    coherent and nothing gets accidentally overwritten.
    # 3) If logresultcsv is provided at the activity level, it
    #    applies to all statements, and the only value values
    #    there are true and false.

### Metrics

- alias.result - A timer which tracks the performance of an op result only.
    This is the async get on the future, broken out as a separate step.
- alias.result-success - A timer that records rate and histograms of the time
    it takes from submitting a query to completely reading the result
    set that it returns, across all pages. This metric is only counted
    for non-exceptional results, while the result metric above includes
    all operations.
- alias.bind - A timer which tracks the performance of the statement
    binding logic, including the generation of data immediately prior
- alias.execute - A timer which tracks the performance of op submission
    only. This is the async execution call, broken out as a separate step.
- alias.tries - A histogram of how many tries were required to get a
    completed operation
- alias.pages - A timer which tracks the performance of paging, specific
    to more than 1-page query results. i.e., if all reads return within 1
    page, this metric will not have any data.
- alias.strides - A timer around each stride of operations within a thread
- alias.skipped-tokens - A histogram that records the count and cycle values
    of skipped tokens.

## YAML Examples

Please see the bundled activities with nosqlbench for examples.
