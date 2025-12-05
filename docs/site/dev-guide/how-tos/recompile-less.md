---
title: Recompile Less
description: Recompile-Less
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
weight: 56
---

If you are working on the main nosqlbench code base, you don't have to rebuild everything. There 
are a couple basic methods you can use to focus just on the module you are developing.

# Dev Iteration

## Maven Install

For driver development, it's best to build and install the existing artifacts from main to your Maven repo. 
This puts all the libraries into your local maven artifact repo (typically under $HOME/.m2/repo) 
and then allows you to build only the specific module you are working in.

```shell
# ensure you have java 17 on your path
java -version
# OR you have a java 17 in $JAVA_HOME/bin/java -version
$JAVA_HOME/bin/java -version
cd nosqlbench
mvn install
```

You can then run mvn only in the module that you need.

```shell
cd adapter-mynewdriver
mvn verify
```

# Module Testing

## NBLIBDIR

Another way to save time is to build the `nbr` module, which is the _NoSQLBench Runtime Module_. 
This comes in jar form and executable form, but it is not built by default. To enable it, you'll 
need to activate its run profile. You can either provide -Pnbr to mvn commands like `mvn -Pnbr 
install`, or set it in your IDE's Maven profile toggles.

Once you have nbr built, you can use it along with any DriverAdapter jars by setting the 
`NBLIBDIR` environment variable.

When this is set, it is split by colon, just as the $PATH variable would be.
For each path, relative to the current working directory, 
1. If it is a directory, then each jar file found under it is added to the classpath.
2. If it is a zip file, then each jar file found within it is added to the classpath.
3. If it is a jar file, then it is added directly to the class path.

This allows you to provide your driver in a separate jar and invoke it with the NoSQLBench 
runtime just as if it were bundled.
