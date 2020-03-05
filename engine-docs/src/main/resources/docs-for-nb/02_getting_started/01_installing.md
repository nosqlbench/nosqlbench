---
title: 01 Installing
weight: 1
---

# 1. Installing DSBench

If you are viewing this via the guidebook, you've already completed this step and you can move on to the next section.

If you are viewing this documentation as exported from the guidebook, then you need to get the binary or jar for your system.

The binary is recommended, since it contains its own built-in JVM. If you are running Linux, get the nosqlbench binary for Linux.

If you are running another system with a supported JVM, then you can do the following:

1. Download nosqlbench.jar
2. Download and install the JVM corresponding to the nosqlbench version. (The second number of the nosqlbench version indicates the JVM version). For example, nosqlbench version 2.13.4 would require JVM 13.
3. Execute nosqlbench as `java -jar nosqlbench.jar ...`. You can replace the elipses `...` with any valid nosqlbench command line.

If you have any trouble, check the troubleshooting section.


## Sanity Check

To ensure that nosqlbench runs on your system, simply run it as

    nb --version


