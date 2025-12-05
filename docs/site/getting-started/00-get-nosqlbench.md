---
title: Requirements
description: Download nb5 and verify your environment.
audience: user
diataxis: howto
tags:
- site
- docs
component: site
topic: docops
status: live
owner: '@nosqlbench/docs'
generated: false
weight: 100
---

NoSQLBench version 5 is packaged directly as a Linux binary named
[nb5](https://github.com/nosqlbench/nosqlbench/releases/latest/download/nb5)
and as an executable Java 17 jar named
[nb5.jar](https://github.com/nosqlbench/nosqlbench/releases/latest/download/nb5.jar).
All releases are available on the
[NoSQLBench Releases](https://github.com/nosqlbench/nosqlbench/releases)
page. The Linux binary is recommended because it bundles its own JVM and removes the need to manage
Java downloads.

## Requirements

The nb5 binary requires Linux and a system with a working
[FUSE](https://en.wikipedia.org/wiki/Filesystem_in_Userspace) library. Most modern distributions
have this out of the box.

`nb5.jar` is not particular about what system you run it on, as long as you have Java 17 or newer.[^1]

## Download Scripts

### Get the latest nb5 binary

```bash
# download the latest nb5 binary and make it executable
curl -L -O https://github.com/nosqlbench/nosqlbench/releases/latest/download/nb5
chmod +x nb5
./nb5 --version
```

👉 If you get an error when executing nb5, consult the
[AppImage troubleshooting page](https://docs.appimage.org/user-guide/run-appimages.html#troubleshooting).

### Get the latest nb5 jar

```bash
# download the latest nb5 jar
curl -L -O https://github.com/nosqlbench/nosqlbench/releases/latest/download/nb5.jar
java -jar nb5.jar --version
```

This guide assumes you are using the Linux binary and initiating commands with `./nb5`. If you are
using the jar, replace `./nb5` with `java -jar nb5.jar` when running commands.

## Running nb5

To run a simple built-in workload:

```bash
./nb5 examples/bindings-basics
```

This runs a built-in scenario located in the workload template named `bindings-basics`. The scenario
is named `default`, so you don’t have to specify it—but you could run it explicitly with
`./nb5 examples/bindings-basics default`.

## Options

Here is a more detailed command which demonstrates how customizable nb5 is:

```bash
./nb5 examples/bindings-basics default \
 filename=exampledata.out \
 format=csv \
 cycles=10000 \
 rate=100 \
 --progress console:1s
```

Each line does something specific:

1. Starts the scenario named `default` from the workload template `examples/binding-basics`.
2. Sets the filename parameter (part of the `stdout` driver) to `exampledata.out`.
3. Sets the output format (part of the `stdout` driver) to CSV.
4. Sets the number of cycles to run to 10,000 (0..10000, representing 0 through 9999).
5. Sets the cycle rate to 100 per second.
6. Tells nb5 to report activity progress to the console every second.

## Dashboards

_TBD_

[^1]: The version scheme for NoSQLBench is `[major]-[java-lts]-[minor]`, so nb5 version 5.17.1
requires Java 17.
