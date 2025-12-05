---
title: Dependencies
description: Dependencies
tags:
- site
- docs
audience: user
diataxis: howto
component: site
topic: docops
status: live
owner: '@nosqlbench/docs'
generated: false
weight: 50
---

With NoSQLBench 5 and newer, there are some compatibility standards that we observe for 
dependencies. This is to ensure that various drivers and their upstream dependencies can work 
harmoniously in one runtime.

## Java LTS

The latest Java LTS release should be used for every new major version. In some cases, newer Java 
LTS releases can be pulled into a minor release after sufficient testing. 

The middle version number indicates the Java platform standard. For example nb5 version
5.**17**.0 uses Java **17**, which is the latest LTS version. The NoSQLBench project will use 
this version across the board, including JVM and language features.

## No Shading

Keeping the project modular and easy to build is essential. In the past, there were some driver 
modules which would not play well together in the same runtime due to JNI or JNA level artifact 
conflicts. Guava, for example was used variously across many drivers and was an unending source 
of dependency conflicts. 

Going forward, any module or dependency which requires shading in order to work with the other 
modules that do not will *not* be included in the project. This is a necessary minimum standard 
to protect the sanity of the code base as well as the developers who work on it. 

## JPMS Modular jars

Ideally, any dependencies which are added have the necessary minimum module information to 
function as a JPMS modular jar. This is *not* requiring the full adoption of JPMS. It is a 
fairly minor task to make your artifacts work this way.

## Logging Subsystems

The standard logging implementation in NoSQLBench is Log4J2. This was used because of its 
extensive runtime configuration support. Further, API stub implementations for SLF4J and others
are included, but only using the later (JPMS-friendly) versions. Included modules *should not* 
implement their own logging subsystem, but instead should either use Log4J2 directly, or SLF4J 
using a modern version.

## Dependency Management

If you are building a new module, be sure you understand the project layout. Leverage the 
existing module structure to minimize new library dependencies. If you are using a common 
library, there is a good chance it is already available and under version management in the 
`mvn-defaults/pom.xml` file. If it is not, then scan for the dependency and hoist it up to the 
mvn-defaults level if that makes sense for multiple modules depending on it. 

# Abandoned Code

If code is contributed which needs to be maintained by a vendor or other party, there will be an 
expectation that the contributor is at least willing to do bug fixes or resolve other 
user-affecting issues. If a module is considered abandoned for a significant period of time, it 
may be removed from the project. 
