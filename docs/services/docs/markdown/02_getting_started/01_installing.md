---
title: 01 Installing
weight: 1
---

# 1. Installing DSBench

If you are viewing this via the guidebook, you've already completed this step and you can move on to the next section.

If you are viewing this documentation as exported from the guidebook, then you need to get the binary or jar for your system.

The binary is recommended, since it contains its own built-in JVM. If you are running Linux, get the dsbench binary for Linux.

If you are running another system with a supported JVM, then you can do the following:

1. Download dsbench.jar
2. Download and install the JVM corresponding to the dsbench version. (The second number of the dsbench version indicates the JVM version). For example, dsbench version 2.13.4 would require JVM 13.
3. Execute dsbench as `java -jar dsbench.jar ...`. You can replace the elipses `...` with any valid dsbench command line.

If you have any trouble, check the troubleshooting section.


## Sanity Check

To ensure that dsbench runs on your system, simply run it as

    dsbench --version


