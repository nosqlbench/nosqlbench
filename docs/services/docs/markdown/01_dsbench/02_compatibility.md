---
title: Compatibility
weight: 3
---

# Binary Format

DSBench is distributed primarily as a binary for Linux distributions. The DSBench binary includes its own OpenJDK runtime. It should work for most modern Linux distributions. The only requirement is that fuse be installed (it is usually already present) on the client system.

# Supported Systems

DSBench runs on Linux as a binary distribution. Any modern Linux which can run AppImage binaries should work.

# Activity Types

In dsbench terms, this means:

Activity types are how DSBench gets its support for different protocols or client drivers. The initial release of DSBench includes support for
these activity types:

- The CQL activity type
  - The initial release of the CQL activity type uses the DataStax driver version 1.9.0
- The stdout activity type.


