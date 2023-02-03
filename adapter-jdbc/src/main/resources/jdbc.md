---
weight: 0
title: jdbc
---

# JDBC driver
This JDBC driver leverages HikariCP for connection pool and works with PostgreSQL. This leverages NoSQLBench based workload generation and performance testing against any PostgreSQL-compatible database cluster. Example: CockroachDB or YugabyteDB (YSQL API).

# Executing JDBC Workload
The following is an example of invoking a JDBC workload.
```yaml
<nb_cmd> run driver=jdbc workload="/path/to/workload.yaml" cycles=1000 threads=100 url="jdbc:postgresql://" serverName=localhost portNumber=5432 databaseName=defaultdb ... -vv --show-stacktraces
```
In the above NB command, following are JDBC driver specific parameters:
* `url`: URL of the database cluster. Default is `jdbc:postgresql://`.
* `serverName`: Default is `localhost`.
* `portNumber`: Default is `5432`.
* `serverName`: The database name. The default is to connect to a database with the same name as the user name used to connect to the server.

Other NB engine parameters are straight forward:
* `driver`: must be `jdbc`
* `threads`: depending on the workload type, the NB thread number determines how many clients will be created. All the clients will share the Connection originated from the Hikari Connection Pool.
* `*.yaml`: the NB jdbc scenario definition workload yaml file

# Configuration
## Config Sources
### Examples



