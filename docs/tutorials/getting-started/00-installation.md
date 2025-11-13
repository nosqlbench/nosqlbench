+++
title = "Installation"
description = "Download and install NoSQLBench on your system"
weight = 100
template = "docs-page.html"

[extra]
quadrant = "tutorials"
topic = "getting-started"
category = "installation"
tags = ["installation", "download", "setup", "prerequisites"]
testable = true
+++

# Download and Install NoSQLBench

NoSQLBench version 5 is available as a Linux binary and as an executable Java JAR file.

## Download Options

### Option 1: Linux Binary (Recommended)

The **nb5 binary** is recommended because it includes its own JVM, eliminating Java installation requirements.

**Direct download:**
- [nb5 (latest)](https://github.com/nosqlbench/nosqlbench/releases/latest/download/nb5)
- [All releases](https://github.com/nosqlbench/nosqlbench/releases)

**Download script:**

```bash
# Download the latest nb5 binary and make it executable
curl -L -O https://github.com/nosqlbench/nosqlbench/releases/latest/download/nb5
chmod +x nb5
./nb5 --version
```

**Requirements:**
- Linux system
- FUSE library (included in most modern distributions)

If you encounter errors running the binary, consult the [AppImage troubleshooting page](https://docs.appimage.org/user-guide/run-appimages.html#troubleshooting).

### Option 2: Java JAR

The **nb5.jar** file runs on any platform with Java 17 or newer.

**Direct download:**
- [nb5.jar (latest)](https://github.com/nosqlbench/nosqlbench/releases/latest/download/nb5.jar)

**Download script:**

```bash
# Download the latest nb5 jar
curl -L -O https://github.com/nosqlbench/nosqlbench/releases/latest/download/nb5.jar
java -jar nb5.jar --version
```

**Requirements:**
- Java 17 or newer (Java 21 recommended)
- Any operating system (Linux, macOS, Windows)

## Version Scheme

NoSQLBench uses the version scheme: `[major]-[java-lts]-[minor]`

Example: **nb5 version 5.17.1** requires Java version 17 (LTS).

## Running NoSQLBench

### Using the Binary

```bash
./nb5 <command> [options]
```

### Using the JAR

```bash
java -jar nb5.jar <command> [options]
```

**Note:** This documentation uses `./nb5` in examples. If you're using the JAR, replace `./nb5` with `java -jar nb5.jar`.

## Verify Installation

Run a simple built-in workload to verify everything works:

```bash
./nb5 examples/bindings-basics
```

This runs a basic scenario that demonstrates data generation without requiring a database connection.

## Common Options

```bash
# Show version
./nb5 --version

# List all built-in workloads
./nb5 --list-workloads

# List all available drivers
./nb5 --list-drivers

# Show help
./nb5 --help

# Copy a workload to local directory for customization
./nb5 --copy cql-starter
```

## Next Steps

Now that NoSQLBench is installed:

1. **[Set Up Test Target](01-test-target.md)** - Start a database to test against
2. **[Run Built-in Scenarios](02-scenarios.md)** - Explore pre-built workloads
3. **[CQL Quickstart](../workloads/cql-quickstart.md)** - Run your first Cassandra workload

## Troubleshooting

**Binary won't execute:**
- Ensure FUSE is installed: `sudo apt-get install fuse` (Ubuntu/Debian)
- Check file permissions: `chmod +x nb5`
- See [AppImage troubleshooting](https://docs.appimage.org/user-guide/run-appimages.html#troubleshooting)

**JAR won't run:**
- Verify Java version: `java -version` (must be 17+)
- Download Java: [OpenJDK](https://openjdk.org/) or [Azul Zulu](https://www.azul.com/downloads/)

**Command not found:**
- Use `./nb5` (with `./` prefix) when in the same directory
- Or move to a directory in your PATH: `sudo mv nb5 /usr/local/bin/`
