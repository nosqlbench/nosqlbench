---
title: Scripting Extensions    
weight: 32
menu:
  main:
    parent: Dev Guide
    identifier: Scripting Extensions
    weight: 12
---

## Requirements

- Java 8
- Maven dependency:

## Scripting Extensions

When a new scripting environment is initialized in nosqlbench, a new instance of each scripting extension is published into it as a variable. This variable acts as a named service endpoint within the scripting environment. For example, an extension for saving a JSON map to disk could be published into the scripting environment as "savejson", and you might invoke it as "savejson.save('somefile.json',myjson);".

## Loading Scripting Extensions

In order to share these with the nosqlbench runtime, the ServerLoader API is used. The scripting environment will load every plugin implementing the SandboxPluginData interface, as long as it has the proper data in META-INF/services/ in the classpath. There are examples of how to do this via Maven in the source repo under the nb-extensions module.

## Maven Dependencies

~~~
<dependency>
  <groupId>io.nosqlbench</groupId>
  <artifactId>nb-api</artifactId>
  <version>1.0.17</version>
  <type>pom</type>
</dependency>
~~~
