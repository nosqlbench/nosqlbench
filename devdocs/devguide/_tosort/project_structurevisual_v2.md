---
title: "NoSQLBench Module Dependencies"
description: "Developer note: NoSQLBench Module Dependencies."
audience: developer
diataxis: explanation
tags:
  - devdocs
component: core
topic: contributing
status: draft
owner: "@nosqlbench/devrel"
generated: false
---

# NoSQLBench Module Dependencies

This is viewable in IntelliJ markdown preview with the optional plugins
enabled.

```plantuml

digraph Test {
  compound=true

  rankdir=LR;
  node[shape=none]
  node[shape="box"]

  subgraph cluster1 {
   label="drivers"
   adapter_amqp[label="adapter-amqp"];
  }

  subgraph cluster0 {
   label="engine"
  }

  subgraph cluster3 {
   label="virtdata-userlibs"
   virtdata_lib_curves4[label="virtdata-lib-curves4"];
   virtdata_lib_realer[label="virtdata-lib-realer"];
   virtdata_lib_random[label="virtdata-lib-random"];
   virtdata_realdata[label="virtdata-realdata"];
   virtdata_lib_basics[label="virtdata-lib-basics"];
   virtdata_api[label="virtdata-api"];
   virtdata_lang[label="virtdata-lang"];
  }

  /**
  nb[label="nb"];
  **/

  driver_tcp -> driver_stdout;
  driver_cqlverify -> driver_cql_shaded;
  driver_kafka -> driver_stdout;

  virtdata_api -> nb_api;
  virtdata_api -> virtdata_lang;
  virtdata_lib_basics -> virtdata_api [ltail=cluster3];
  virtdata_lib_random -> virtdata_lib_basics
  virtdata_lib_curves4 -> virtdata_lib_basics;

  /**
  mvndefaults[label="mvn-defaults"];
  mvndefaults -> TESTDEPS;
  **/

   virtdata_lib_realer -> virtdata_lib_basics;

  /**
  virtdata_userlibs -> virtdata_realdata;
  virtdata_userlibs -> virtdata_lib_realer;
  virtdata_userlibs -> virtdata_lib_random;
  virtdata_userlibs -> virtdata_lib_basics;
  virtdata_userlibs -> virtdata_lib_curves4;
  virtdata_userlibs -> docsys;
  **/

}
```
