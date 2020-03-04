---
title: Troubleshooting
weight: 05
---

# Troubleshooting

This section will contain some troubleshooting guidance for
common issue as we uncover them.

## Errors while starting dsbench binary

If you get an error while trying to run the Linux DSBench binary, ensure that you have the system module installed for fuse. This module is used by the AppImage runtime that allows for a bundled binary.

## Errors when running java -jar

### Verify java binary path

You will need to make sure that the java binary is the correct one that is being run. Either call it with the full path `/usr/local/...` or use `which java` to see which java executable is used when you just run `java ...`.

### Verify java version

Each version of dsbench requires a particular major version of Java. For example, dsbench version 2.12.26 requires at least Java 12.
You can quickly check which version of java you have on your path with `java -version`