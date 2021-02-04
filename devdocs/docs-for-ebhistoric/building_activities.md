---
title: Building ActivityTypes
weight: 32
menu:
  main:
    parent: Dev Guide
    identifier: Building ActivityTypes
    weight: 12
---


## Requirements

- Java 8
- Maven


## Building new Driver Types

1. Add the nosqlbench API to your project via Maven:

~~~
<dependency>
  <groupId>io.nosqlbench</groupId>
  <artifactId>engine-api</artifactId>
  <version>1.0.17</version>
  <type>pom</type>
</dependency>
~~~

2. Implement the ActivityType interface. Use the [Annotated Diag ActivityType] as a reference point as needed.
3. Add your new ActivityType implementation to the nosqlbench classpath.
4. File Issues against the [nosqlbench Project](http://github.com/nosqlbench/nosqlbench/issues) for any doc or API enhancements that you need.

## Working directly on nosqlbench

You can download and locally build nosqlbench. Do this if you want contribute
or otherwise experiment with the nosqlbench code base.

1. Get the source:
~~~
git clone http://github.com/nosqlbench/nosqlbench.git
~~~

2. Build and install locally:
~~~
pushd nosqlbench # assumes bash
mvn clean install
~~~

This will install the nosqlbench artifacts to your local _~/.m2/repository_.


## Using ActivityTypes

There are a couple ways you can use your new ActivityTypes  with the nosqlbench
runtime. You can mix and match these as needed. The most common way to integrate
your ActivityTypes with the nosqlbench core is with Maven, but the details on
thi will vary by environment.


