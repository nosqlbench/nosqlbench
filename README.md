![maven build](https://github.com/nosqlbench/nosqlbench/workflows/CI/badge.svg)

This project combines upstream projects of engineblock and virtualdataset into one main project. More details on release practices and contributor guidelines are on the way.

# Status

This is in active development. Collaborators are welcome. However, there is still work to be done to groom the slope for new users.

# Project Structure

NoSQLBench is a large project. It has lots of advanced functionality built-in that you won't find in any other performance testing tool.

To make it easier to find the module you are looking for (for developers),
the following strict naming conventions have been adopted:

- All original packages of this project start with `io.nosqlbench`.
- All original packages within each module are based directly on the above root package and the module name together. For example, the 'engine-api' module contains packages in `io.nosqlbench.engine.api`.

The procedural generation support that has been imported from the VirtualDataSet project now lives in virtdata-* modules.
The core runtime that used to be called EngineBlock now lives in engine-* modules.

The binary packaging support for Linux (with AppImage) is contained in the nb module. It provides a binary `nb` which is the main command-line artifact from this project.

The defaults that are used by all modules are contained in the mvn-defaults module.


## System Compatibility

This is a Linux targeted tool, as most cloud/nosql testing is done on Linux instances. Some support for other systems is available, but more work is needed to support them fully. Here is what is supported for each:

1. on Linux, all features are supported, for both `nb.jar` as well as the appimage binary `nb`
2. on Mac, with `nb.jar` all features are supported, except --docker-metrics
3. On Windows, with `nb.jar` all features are supported, except --docker-metrics

## nosqlbench

This project aims to provide a missing power tool in the test tooling arsenal.

The design goals:

1. Provide a useful and intuitive Reusable Machine Pattern for constructing and reasoning about concurrent performance tests. To encourage this, the runtime machinery is based on [simple and tangible core concepts](http://docs.nosqlbench.io/user-guide/concepts/).
2. Reduce testing time of complex scenarios with many variables. This is achieved by controlling tests from an [open javascript sandbox](http://docs.nosqlbench.io/user-guide/scripting/). This makes more sophisticated scenarios possible when needed.
3. Minimize the amount of effort required to get empirical results from a test cycle. For this, [metrics reporting](http://docs.nosqlbench.io/user-guide/metrics/) is baked in.

In short, NoSQLBench wishes to be a programmable power tool for performance
testing. However, it is somewhat generic. It doesn't know directly about a
particular type of system, or protocol. It simply provides a suitable machine
harness in which to put your drivers and testing logic. If you know how to build
a client for a particular kind of system, EB will let you load it like a plugin
and control it dynamically.

The most direct way to do this, if you are a tool developer, is to build your
own activity type drivers and embed EB as the core runtime. You can always
experiment with it and learn how it works by using the built-in diagnostic
drivers.

## History

The NoSQLBench project started as a branch of [test
client](http://github.com/jshook/testclient). It has since evolved to be more generic and capable.

## License

nosqlbench is licensed under the Apache Public License 2.0

## Thanks

[![DataStax Logo](https://www.datastax.com/sites/default/files/content/graphics/logo/DS-logo-2019_1-25percent.png)](http://datastax.com/)

This project is sponsored by [DataStax](http://datstax.com/) -- The always-on, active everywhere, distributed hybrid cloud database built on Apache Cassandra™, and designed from the ground up to run anywhere, on any cloud, in any datacenter, and in every possible combination. DataStax delivers the ultimate hybrid and multi-cloud database.

![YourKit Logo](https://www.yourkit.com/images/yklogo.png)

This project uses tools provided by YourKit, LLC. YourKit supports open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>
and <a href="https://www.yourkit.com/.net/profiler/">YourKit .NET Profiler</a>,
innovative and intelligent tools for profiling Java and .NET applications.
