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



