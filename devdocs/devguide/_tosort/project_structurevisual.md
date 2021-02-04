# NoSQLBench Module Dependencies

This is viewable in Intellij markdown preview with the optional plugins
enabled.

```plantuml

digraph Test {
  compound=true

  rankdir=LR;
  node[shape=none]
  node[shape="box"]

  subgraph cluster1 {
   label="drivers"
   driver_diag[label="driver-diag"];
   driver_stdout[label="driver-stdout"];
   driver_tcp[label="driver-tcp"];
   driver_http[label="driver-http"];
   driver_cql_shaded[label="driver-cql-shaded"];
   driver_cqlverify[label="driver-cqlverify"];
   driver_web[label="driver-web"];
   driver_kafka[label="driver-kafka"];
   driver_mongodb[label="driver-mongodb"];
   driver_jmx[label="driver-jmx"];

  }

  driver_diag -> engine_api [ltail="cluster1"];

  subgraph cluster0 {
   label="engine"
   engine_core[label="engine-core"];
   engine_extensions[label="engine-extensions"];
   engine_docs[label="engine-docs"];
   engine_cli[label="engine-cli"];
   engine_rest[label="engine-rest"];
   engine_docker[label="engine-docker"];
   engine_api[label="engine-api"];
   docsys[label="docsys"];
  }

  engine_api -> drivers_api;
//  subgraph cluster2 {
//   label="APIs"
   nb_api[label="nb-api"];
   nb_annotations[label="nb-annotations",tooltip="sdf"];
   nb_api -> nb_annotations;
  //}

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

  docsys -> nb_api;


  engine_api -> nb_api;
  engine_api -> nb_annotations;
  engine_api -> virtdata_lib_basics[lhead="cluster3"];
  engine_core -> engine_api [ltail="cluster0"];
  engine_docs -> docsys;
  engine_cli -> engine_core;
  engine_cli -> engine_docker;
  engine_rest -> engine_cli;



  /**
  nb[label="nb"];
  nb -> driver_web;
  nb -> driver_kafka;
  nb -> driver_stdout;
  nb -> driver_diag;
  nb -> driver_tcp;
  nb -> driver_http;
  nb -> driver_jmx;
  nb -> driver_cql_shaded;
  nb -> driver_cqlverify;
  nb -> driver_cql_mongodb;
  nb -> engine_rest;
  nb -> engine_cli;
  nb -> engine_docs;
  nb -> engine_core;
  nb -> engine_extensions;
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
