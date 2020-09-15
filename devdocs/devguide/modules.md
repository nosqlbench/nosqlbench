# test

```plantuml

digraph Test {

  rankdir=TB;
  node[shape=none]

  node[shape="box"]

  docsys[label="docsys"];
  docsys -> nb_api;


  nb_annotations[label="nb-annotations"];

  engine_api[label="engine-api"];
  engine_api -> nb_api;
  engine_api -> nb_annotations;
  engine_api -> virtdata_userlibs;

  engine_core[label="engine-core"];
  engine_core -> engine_api;

  engine_extensions[label="engine-extensions"];
  engine_docker[label="engine-docker"];
  engine_docker -> engine_api;


  engine_docs[label="engine-docs"];
  engine_docs -> docsys;

  engine_cli[label="engine-cli"];
  engine_cli -> engine_core;
  engine_cli -> engine_docker;

  engine_rest[label="engine-rest"];
  engine_rest -> engine_cli;

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



  driver_diag[label="driver-diag"];
  driver_diag -> engine_api;

  driver_stdout[label="driver-stdout"];
  driver_stdout -> engine_api;

  driver_tcp[label="driver-tcp"];
  driver_tcp -> engine_api;
  driver_tcp -> driver_stdout;

  driver_http[label="driver-http"];
  driver_http -> engine_api;

  driver_cql_shaded[label="driver-cql-shaded"];
  driver_cql_shaded -> engine_api;

  driver_cqlverify[label="driver-cqlverify"];
  driver_cqlverify -> driver_cql_shaded;

  driver_web[label="driver-web"];
  driver_web -> engine_api;

  driver_kafka[label="driver-kafka"];
  driver_kafka -> engine_api;
  driver_kafka -> driver_stdout;

  driver_mongodb[label="driver-mongodb"];
  driver_mongodb -> engine_api;

  driver_jmx[label="driver-jmx"];
  driver_jmx -> engine_api;

  virtdata_api[label="virtdata-api"];
  virtdata_api -> nb_api;
  virtdata_api -> virtdata_lang;

  virtdata_lang[label="virtdata-lang"];

  virtdata_realdata[label="virtdata-realdata"];
  virtdata_realdata -> virtdata_api;

  virtdata_lib_basics[label="virtdata-lib-basics"];
  virtdata_lib_basics -> virtdata_api;

  virtdata_lib_random[label="virtdata-lib-random"];
  virtdata_lib_random -> virtdata_api
  virtdata_lib_random -> virtdata_lib_basics

  virtdata_lib_curves4[label="virtdata-lib-curves4"];
  virtdata_lib_curves4 -> virtdata_api;
  virtdata_lib_curves4 -> virtdata_lib_basics;

  mvndefaults[label="mvn-defaults"];
  mvndefaults -> TESTDEPS;

  virtdata_lib_realer[label="virtdata-lib-realer"];
  virtdata_lib_realer -> virtdata_lib_basics;

  nb_api[label="nb-api"];
  nb_api -> nb_annotations;

  virtdata_userlibs[label="virtdata-userlibs"];
  virtdata_userlibs -> virtdata_realdata;
  virtdata_userlibs -> virtdata_lib_realer;
  virtdata_userlibs -> virtdata_api;
  virtdata_userlibs -> virtdata_lib_random;
  virtdata_userlibs -> virtdata_lib_basics;
  virtdata_userlibs -> virtdata_lib_curves4;
  virtdata_userlibs -> docsys;
}
```
