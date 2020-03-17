![build](https://github.com/nosqlbench/nosqlbench/workflows/build/badge.svg)

# NoSQLBench

**The Open Source, Pluggable, NoSQL Benchmarking Suite**

## What is NoSQLBench?

NoSQLBench is a serious performance testing tool for the NoSQL ecosystem.
It brings together features and capabilities that are not found in any
other tool.

- You can run common testing workloads directly from the command line. You
  can start doing this within 5 minutes of reading this.
- You can generate virtual data sets of arbitrary size, with deterministic
  data and statistically shaped values.
- You can design custom workloads that emulate your application, contained
  in a single file, based on statement templates - no IDE or coding required.
- You can immediately plot your results in a docker and grafana stack on Linux
  with a single command line option.
- When needed, you can open the access panels and rewire the runtime behavior
  of NoSQLBench to do advanced testing, including a full scripting environment
  with Javascript.

The core machinery of NoSQLBench has been built with attention to detail.
It has been battle tested within DataStax as a way to help users validate their
data models, baseline system performance, and qualify system designs for scale.

In short, NoSQLBench wishes to be a programmable power tool for performance
testing. However, it is somewhat generic. It doesn't know directly about a
particular type of system, or protocol. It simply provides a suitable machine
harness in which to put your drivers and testing logic. If you know how to build
a client for a particular kind of system, it will let you load it like a plugin
and control it dynamically.

Initially, NoSQLBench comes with support for CQL, but we would like to see this
expanded with contributions from others.

## Origins

The code in this project comes from multiple sources. The procedural data
generation capability was known before as 'Virtual Data Set'. The core runtime
and scripting harness was from the 'EngineBlock' project. The CQL support was
previously used within DataStax. In March of 2020, DataStax and the project
maintainers for these projects decided to put everything into one OSS project
in order to make contributions and sharing easier for everyone. Thus, the new
project name and structure was launched as nosqlbench.io. NoSQLBench is an
independent project that is sponsored by DataStax.

We offer NoSQLBench as a new way of thinking about testing systems. It is not
limited to testing only one type of system. It is our wish to build a community
of users and practice around this project so that everyone in the NoSQL ecosystem
can benefit from common concepts and understanding and reliable patterns of use.

## Contributing

We are actively looking for contributors to help make NoSQLBench better.
This is an ambitious project that is just finding its stride. If you want
to be part of the next chapter in NoSQLBench development please look at
[CONTRIBUTING](CONTRIBUTING.md) for ideas, and jump in where you feel comfortable.

All contributors are expected to abide by the [CODE_OF_CONDUCT](CODE_OF_CONDUCT.md).

## License

All of the code in this repository is licensed under the APL version 2. If you contribute
to this project, then you must agree to license all of your construbutions under
this license.

## System Compatibility

This is a Linux targeted tool, as most cloud/nosql testing is done on Linux instances. Some support for other systems is available, but more work is needed to support them fully. Here is what is supported for each:

1. on Linux, all features are supported, for both `nb.jar` as well as the appimage binary `nb`
2. on Mac, with `nb.jar` all features are supported, except --docker-metrics
3. On Windows, with `nb.jar` all features are supported, except --docker-metrics

## Thanks

[![DataStax Logo](https://www.datastax.com/sites/default/files/content/graphics/logo/DS-logo-2019_1-25percent.png)](http://datastax.com/)

This project is sponsored by [DataStax](http://datstax.com/) -- The always-on,
active everywhere, distributed hybrid cloud database built on Apache Cassandra™,
and designed from the ground up to run anywhere, on any cloud, in any datacenter,
and in every possible combination. DataStax delivers the ultimate hybrid and multi-cloud database.

![YourKit Logo](https://www.yourkit.com/images/yklogo.png)

This project uses tools provided by YourKit, LLC. YourKit supports open source projects
with its full-featured Java Profiler.  YourKit, LLC is the creator of
<a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>
and <a href="https://www.yourkit.com/.net/profiler/">YourKit .NET Profiler</a>,
innovative and intelligent tools for profiling Java and .NET applications.
