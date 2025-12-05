---
source: nb-adapters/adapter-jdbc/src/main/resources/pgvector.md

title: "pgvector"
description: "Adapter doc for pgvector."
audience: operator
diataxis: howto
tags:
  - jdbc
  - drivers
component: drivers
topic: drivers
status: live
owner: "@nosqlbench/drivers"
generated: false
---

# pgvector

The jdbc adapter supports testing against
[Postgres pgvector](https://github.com/pgvector/pgvector-java)
with a few additional usage notes.

# Executing a pgvector Workload
The following is an example of invoking a pgvector workload.
```shell
<nb_cmd> run driver=jdbc workload="/path/to/workload.yaml" cycles=1000 threads=100 url="jdbc:postgresql://host:port/database" serverName=localhost portNumber=5432 databaseName="defaultdb" user="newuser" password="CHANGE_ME" ssl="true" sslmode="prefer" sslrootcert="/path/to/postgresql_certs/root.crt" -vv --show-stacktraces
```
In the above NB command, following are JDBC driver specific parameters:
* `url`: URL of the database cluster. Default is `jdbc:postgresql://`.
* `serverName`: Default is `localhost`.
* `portNumber`: Default is `5432`.
* `serverName`: The database name. The default is to connect to a database with the same name as the user name used to connect to the server.
* `user`: The user to connect to the database as
* `password`: The password specific to the user
* `ssl`: Optional parameter to control whether ssl is used for communication with the database. Defaults to false
* `sslmode`: Optional parameter for ssl mode to use. If ssl is true defaults to prefer
* `sslrootcert`: Optional parameter specifying the location of the ssl root certificate

## Examples
Examples of workload blocks for a pgvector vector storage database
#### Op Template Examples
````yaml
ops:
  create_table:
      ddl: |
        CREATE TABLE IF NOT EXISTS TEMPLATE(schemaname,public).TEMPLATE(tablename,pgvec)
        (key TEXT PRIMARY KEY, value vector(TEMPLATE(dimensions,5)));
      create_vector_index:
        ddl: |
          CREATE INDEX IF NOT EXISTS idx_TEMPLATE(tablename,pgvec)_TEMPLATE(indextype)_TEMPLATE(similarity_function)
          ON TEMPLATE(schemaname,public).TEMPLATE(tablename,pgvec)
          USING TEMPLATE(indextype) (value vector_TEMPLATE(similarity_function)_ops)
          WITH (TEMPLATE(indexopt));
  main_insert:
    dmlwrite: |
      INSERT INTO TEMPLATE(schemaname,public).TEMPLATE(tablename,pgvec) VALUES (?,?) ON CONFLICT DO NOTHING;
    prep_stmt_val_arr: |
      {rw_key},{train_floatlist}
  main_select:
    # NOTE: right now this is only for cosine similarity.
    #       in pgvector, '<=>' is for cosine similarity
    #                    '<->' is for euclidean distance
    #                    '<#>' is for inner product
    dmlread: |
      SELECT key, (value <=> ?) as score
      FROM TEMPLATE(schemaname,public).TEMPLATE(tablename,pgvec)
      ORDER BY score ASC
      LIMIT TEMPLATE(top_k,100);
    prep_stmt_val_arr: |
      {test_floatlist}
    verifier-key: "key"
    verifier-init: |
      relevancy=new io.nosqlbench.nb.api.engine.metrics.wrappers.RelevancyMeasures(_parsed_op)
      k=TEMPLATE(top_k,100)
      relevancy.addFunction(io.nosqlbench.engine.extensions.computefunctions.RelevancyFunctions.recall("recall",k));
      relevancy.addFunction(io.nosqlbench.engine.extensions.computefunctions.RelevancyFunctions.precision("precision",k));
    verifier: |
      // driver-specific function
      actual_indices=pgvec_utils.getValueListForVerifierKey(result);
      // driver-agnostic function
      relevancy.accept({relevant_indices},actual_indices);
      return true;
````
