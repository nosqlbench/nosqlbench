# cqld4

This is the newly revamped driver for CQL which uses the DataStax OSS Driver version 4. As
there was a significant restructuring of the APIs between CQL driver 4.x and previous versions, this
driver is a clean and separate implementation which aims to use the features of version 4.x of the
native driver directly as well as new internal NoSQLBench APIs.

This means that many features that advanced testers may have been used to (the syntactical sugar,
the surfacing of advanced configuration properties in simple ways, and so on) will have to be
redesigned to fit with version 4 of the driver. Most users who do basic testing with direct CQL
syntax should see few issues, but advanced testers will need to consult this documentation
specifically to understand the differences between `cqld4` driver features and `cql` driver
features.

Notably, these features need to be re-built on the cqld4 driver to bring it up to parity with
previous advanced testing features:

- verify
- result set size metrics
- explicit paging metrics

## Configuration

The DataStax Java Driver 4.* has a much different configuration system than previous versions. For
changing driver settings with this version it is **highly recommended** that users use the built-in
driver settings and configuration file/profile options, just as they would for an application. This
serves two goals: 1) to ensure that the settings you test with are portable from test environment to
application, and 2) to allow you to configure driver settings directly, without depending on
internal helper logic provided by NoSQLBench. This means that the driver options exposed are those
provided by the low-level driver, thus removing another dependency from your test setup.

### Config Sources

By using the option `driverconfig`, you can have as many configuration sources as you like, even
mixing in JSON or remote URLs.

**examples**

Configure directly from a config file, or classpath resource:

```shell
# If this isn't found in the file system, the classpath will also be checked.
nb5 ... driverconfig=myconfig.json
```

Configure directly from JSON:
```shell
nb5 ... driverconfig='{basic.request.timeout:"2 seconds"}'
```

Configure directly form a remote URL:
```shell
nb5 ... driverconfig='http://gist.github.com...'
```

### Basic Cqld4 driver options

- **hosts** & **localdc** - (required unless using scb) - Set the endpoint and local datacenter name
  for the driver.
  - example: `host=mydsehost localdc=testdc1`
- **driverconfig** - (explained above) - set the configuration source for the driver.
- **username** OR **userfile** - (optional, only one may be used) - If you need to specify a
  username but want to put it in a file instead, simply use the `userfile=myfile` option. It is
  not uncommon to say `userfile=userfile`.
* **password** OR **passfile** - (optional, only one may be used) - Fi you need to specify a
  password but want to put it ina  file instead, simply use the `passfile=mypassfile` option. It
  is not uncommon to say `passfile=passfile`.
* **showstmt** - enable per-statement diagnostics which show as much of the statement as possible
  for the given statement type. *WARNING* - Do not use this for performance testing, only for
  diagnostics.
* **maxpages** - configure the maximum number of pages allowed in a CQL result set. This is
  configured to `maxpages=1` by default, so that users will be aware of any paging that occurs
  by default. If you expect and want to allow paging in your operation, then set this number
  higher. A *synthetic* exception is generated as `UnexpectedPagingException` by default when
  the number of pages exceeds maxpages.

### Activity level Driver Config

The activity parameters which are provided by the driver are exposed as `driver.<name>`. Any
configuration option that is specified this way will be applied directly to the driver through the
type-safe configuration layer. For example, specifying `driver.basic.request.timeout='2 seconds'`
has the same effect as setting `basic.request.timeout` in a driver configuration file.

## Backwards Compatibility with `cql` and `cqld3`

Many driver options were provided in a more convenient form for testing in previous CQL drivers with
NoSQLBench. Due to the changes in driver 4.x, the implementation of these options had to change as
well. Where possible, a backwards-compatible option helper was provided so that test defined for
`cql` and `cqld3` drivers would just work with the `cqld4` driver. In some cases, this simply was
not possible as some options were no longer supported, or changed so much that there was no longer a
direct mapping that would work consistently across versions. You can try to use the previous
options, like `pooling` and so on. If the option is not supported as such, it will cause an error
with an explanation. Otherwise, these helper options will simply set the equivalent options in the
driver profile to achieve the same effect. As stated above, it is highly recommended that driver
settings be captured in a configuration file and set with `driverconfig=<file>.json`

## Statement Forms

The CQLd4 driver supports idiomatic usage of all the main statement APIs within the native Java
driver. The syntax for specifying these types is simplified as well, using only a single
`type` field which allows values of simple, prepared, raw, gremlin, fluent, and so on. The previous
form of specifing `type: cql` and optional modifiers like `prepared` and
`parameterized` is deprecated now, sinces all the forms are explicitly supported by a well-defined
type name.

The previous form will work, but you will get a warning, as these should be deprecated going
forward. It is best to use the forms in the examples below. The defaults and field names for the
classic form have not changed.

## CQLd4 Op Template Examples

```yaml
ops:

  # prepared statement
  # allows for parameterization via bindings, and uses prepared statements internally
  example-prepared-cql-stmt:
    prepared: |
     select one, two from buckle.myshoe where ...

  # prepared statement (verbose form)
  example-prepared-cql-stmt-verbose:
    type: prepared
    stmt: |
      select one, two from buckle.myshoe where ...

  # simple statement
  # allows for parameterization via bindings, but does not use prepared statements internally
  example-simple-cql-stmt:
    simple: |
     select three, four from knock.onthedoor where ...

  # raw statement
  # pre-renders the statement into a string, with no driver-supervised parameterization
  # useful for testing variant DDL where some fields are not parameterizable
  # NOTE: the raw form does its best to quote non-literals where needed, but you may
  # have to inject single or double quotes in special cases.
  example-raw-cql-stmt:
    raw: |
     create table if not exist {ksname}.{tblname} ...

  # gremlin statement using the fluent API, as it would be written in a client application
  example-fluent-graph-stmt:
    fluent: >-
      g.V().hasLabel("device").has("deviceid", UUID.fromString({deviceid}))
    # if imports are not specified, the following is auto imported.
    # if imports are specified, you must also provide the __ class if needed
    imports:
     - org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__

  # gremlin statement using string API (not recommended)
  example-raw-gremlin-stmt:
    gremlin: >-
      g.V().hasLabel("device").has("deviceid", UUID.fromString('{deviceid})')
```

## CQL Op Template - Optional Fields

If any of these are provided as op template fields or as op params, or as activity params, then they
will have the described effect. The calls to set these parameters on an individual statement are
only incurred if they are provided. Otherwise, defaults are used. These options can be applied to
any of the statement forms above.

```yaml
params:

  # Set the consistency level for this statement
  # For Astra, use only LOCAL_QUORUM
  # Otherwise, one of
  # ALL|EACH_QUORUM|QUORUM|LOCAL_QUORUM|ONE|TWO|THREE|LOCAL_ONE|ANY
  cl: LOCAL_QUORUM
  # or consistency_level: ...

  # Set the serial consistency level for this statement.
  # Note, you probably don't need this unless you are using LWTs
  # SERIAL ~ QUORUM, LOCAL_SERIAL ~ LOCAL_QUORUM
  scl: LOCAL_SERIAL
  # or serial_consistency_level: ...

  # Set a statement as idempotent. This is important for determining
  # when ops can be trivially retried with no concern for unexpected
  # mutation in the event that it succeeds multiple times.
  # true or false
  idempotent: false

  # Set the timeout for the operation, from the driver's perspective,
  # in seconds. "2 seconds" is the default, but DDL statements, truncate or drop
  # statements will generally need more. If you want milliseconds, just use
  # fractional seconds, like 0.500
  timeout: 2.0

  # Set the maximum number of allowed pages for this request before a
  # UnexpectedPagingException is thrown.
  maxpages: 1

  # Set the LWT rebinding behavior for this statement. If set to true, then
  # any statement result which was not applied will be retried with the
  # conditional fields set to the currently visible values. This makes all LWT
  # statements do another round trip of retrying (assuming the data doesn't
  # match the preconditions) in order to test LWT performance.
  retryreplace: true

  # Set the number of retries allowed by the retryreplace option. This is set
  # to 1 conservatively, as with the maxpages setting. This means that you will
  # see an error if the first LWT retry after an unapplied change was not successful.
  maxlwtretries: 1

  ## The following options are meant for advanced testing scenarios only,
  ## and are not generally meant to be used in typical application-level,
  ## data mode, performance or scale testing. These expose properties
  ## which should not be set for general use. These allow for very specific
  ## scenarios to be constructed for core system-level testing.
  ## Some of them will only work with specially provided bindings which
  ## can provide the correct instantiated object type.

  # replace the payload with a map of String->ByteBuffer for this operation
  # type: Map<String, ByteBuffer>
  custom_payload: ...

  # set an instantiated ExecutionProfile to be used for this operation
  # type: com.datastax.oss.driver.api.core.config.DriverExecutionProfile
  execution_profile: ...

  # set a named execution profile to be used for this operation
  # type: String
  execution_profile_name: ...

  # set a resolved target node to be used for this operation
  # type: com.datastax.oss.driver.api.core.metadata.Node
  node: ...

  # set the timestamp to be used as the "now" reference for this operation
  # type: int
  now_in_seconds: ...

  # set the page size for this operation
  # type: int
  page_size: ...

  # set the query timestamp for this operation (~ USING TIMESTAMP)
  # type: long
  query_timestamp:

  # set the routing key for this operation, as a single bytebuffer
  # type: ByteArray
  routing_key: ...

  # set the routing key for this operation as an array of bytebuffers
  # type: ByteArray[]
  routing_keys: ...

  # set the routing token for this operation
  # type: com.datastax.oss.driver.api.core.metadata.token.Token
  routing_token: ...

  # enable (or disable) tracing for this operation
  # This should be used with great care, as tracing imposed overhead
  # far and above most point queries or writes. Use it sparsely or only
  # for functional investigation
  # type: boolean
  tracing: ...
```

## Driver Cache

Like all driver adapters, the CQLd4 driver has the ability to use multiple low-level driver
instances for the purposes of advanced testing. To take advantage of this, simply set a `space`
parameter in your op templates, with a dynamic value.
__WARNING__: If you use the driver cache feature, be aware that creating a large number of driver
instances will be very expensive. Generally driver instances are meant to be initialized and then
shared throughout the life-cycle of an application process. Thus, if you are doing multi-instance
driver testing, it is best to use bindings functions for the `space` parameter which have bounded
cardinality per host.



