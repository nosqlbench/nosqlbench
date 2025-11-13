+++
title = "Coding Standards"
description = "Code quality standards for NoSQLBench development"
weight = 10
template = "page.html"

[extra]
quadrant = "development"
topic = "standards"
category = "coding"
tags = ["development", "standards", "testing", "code-quality"]
+++

In order to keep the code base tidy, here are the coding standards we try to observe:

# Unit Tests

## Unit Test Coverage

We really like unit tests where it makes sense. It usually makes sense to write unit tests!
However, we aren't chasing 100% code coverage. We *do* measure it, and we do intend to keep it
from going down past some reasonable level. We will start to pull up the code coverage
requirements for each module over time, but only to a reasonable level.

## Unit Test Logging

Don't write to System.stdout or System.stderr in your unit tests. Instead, use a logger, as in

```java
private final static Logger logger = LogManager.getLogger(MyClass.class);
```

The logging subsystem is used for nearly *all* console IO with NoSQLBench. This is a drastic
simplification for developers and maintainer. In general, users don't want to be notified of
anything except when the do, and when they do, they can simply use `-v` to turn the default
console logging level up from `WARN` to `INFO` and so on. This applies as well to tests. We can
keep console IO low for unit tests and even us async logging in the background to speed up
builds and keep the build output tidy for when you need to troubleshoot actual build errors.

## Integrated Test Coverage

Since NB is a layered runtime design, we need to be specific when we are talking about
_integrated_ tests. Once we have stabilized the new integrated test harness, it will be better
documented for contributors. This is a work in progress.
