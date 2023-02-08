# JDBC driver
This JDBC driver leverages [Hikari Connection Pool](https://github.com/brettwooldridge/HikariCP/wiki) for connection pool and works with PostgreSQL®. This leverages NoSQLBench based workload generation and performance testing against any PostgreSQL-compatible database cluster. Example: CockroachDB® or YugabyteDB® (YSQL API).

# Executing JDBC Workload
The following is an example of invoking a JDBC workload.
```shell
<nb_cmd> run driver=jdbc workload="/path/to/workload.yaml" cycles=1000 threads=100 url="jdbc:postgresql://" serverName=localhost portNumber=5432 databaseName=defaultdb ... -vv --show-stacktraces
```
In the above NB command, following are JDBC driver specific parameters:
* `url`: URL of the database cluster. Default is `jdbc:postgresql://`.
* `serverName`: Default is `localhost`.
* `portNumber`: Default is `5432`.
* `serverName`: The database name. The default is to connect to a database with the same name as the user name used to connect to the server.

Other NB engine parameters are straight forward:
* `driver`: *must* be `jdbc`.
* `threads`: depending on the workload type, the NB thread number determines how many clients will be created. All the clients will share the Connection originated from the Hikari Connection Pool.
* `*.yaml`: the NB jdbc scenario definition workload yaml file.
* `<nb_cmd>`: is `./nb` (using binary) or the `java -jar nb5.jar`.

# Configuration
These are the main configurations with which we could issue a query and process the results back based on the [PostgreSQL® Query](https://jdbc.postgresql.org/documentation/query/) pattern.
## Config Sources
* `execute`: This is to issue any DDL statements such `CREATE DATABASE|TABLE` or `DROP DATABASE|TABLE` operations which returns nothing.
* `query`: This is to issue DML statement such as `SELECT` operation which would return a `ResultSet` object to process.
* `update`: This is to issue DML statements such as `INSERT|UPDATE|DELETE` operations that will return how many number of rows were affected by that operation.

## Statement Forms
The syntax for specifying these types is simplified as well, using only a single `type` field which allows values of `execute`, `query`, & `update`
and specifying the raw statements in the `stmt`. Alternatively, one could directly use one of the types and provide the raw query directly.

### Examples
Check out the default activities under the [activities.baselinesv2](./activities.baselinesv2) directory.
#### Op Template Examples
````yaml
ops:
  drop-database:
    type: execute
    stmt: |
      DROP DATABASE IF EXISTS TEMPLATE(database,baselines);
  create-table:
    execute: |
      CREATE TABLE IF NOT EXISTS TEMPLATE(database,baselines).TEMPLATE(table,keyvalue);
  select-table:
    query: |
      SELECT one, two, three FROM TEMPLATE(database,baselines).TEMPLATE(table,keyvalue) WHERE ...;
  insert-table:
    update: |
      UPDATE TABLE TEMPLATE(database,baselines).TEMPLATE(table,keyvalue) SET key = 'value' WHERE ...;
````
