# cqld4 driver

This is the newly revamped (beta) driver for CQL which uses the DataStax OSS Driver version 4. As
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

    # If this isn't found in the file system, the classpath will also be checked.
    driverconfig=myconfig.json

Configure directly from JSON:

    driverconfig='{basic.request.timeout:"2 seconds"}'

Configure directly form a remote URL:

    driverconfig='http://gist.github.com...'

Configure from multiple sources:

    driverconfig=myconfig.json

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
with an explanation. Otherwise, these helper options will simply set the equivalent options
in the driver profile to achieve the same effect. As stated above, it is highly recommended that
driver settings be captured in a configuration file and set with `driverconfig=<file>.json`

## Statement Forms

The CQLd4 driver supports idiomatic usage of all the main statement APIs within the native Java
driver. The syntax for specifying these types is simplified as well, using only a single
`type` field which allows values of simple, prepared, raw, gremlin, fluent, and so on.
The previous form of specifing `type: cql` and optional modifiers like `prepared` and
`parameterized` is deprecated now, sinces all the forms are explicitly supported by a
well-defined type name.

The previous form will work, but you will get a warning, as these should be deprecated
going forward. It is best to use the forms in the examples below. The defaults and field
names for the classic form have not changed.

## CQLd4 Op Template Examples

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

      # gremlin statement using string API (not recommended)
      example-raw-gremlin-stmt:
        gremlin: >-
          g.V().hasLabel("device").has("deviceid", UUID.fromString('{deviceid})')


## Driver Cache

Like all driver adapters, the CQLd4 driver has the ability to use multiple low-level
driver instances for the purposes of advanced testing. To take advantage of this,
simply set a `space` parameter in your op templates, with a dynamic value.
__WARNING__: If you use the driver cache feature, be aware that creating a large
number of driver instances will be very expensive. Generally driver instances are meant
to be initialized and then shared throughout the life-cycle of an application process.



