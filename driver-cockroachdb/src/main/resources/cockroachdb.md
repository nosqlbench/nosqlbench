# CockroachDB Driver

This is a driver for CockroachDB. It extends the generic JDBC Driver and
inherits its parameters.

### CockroachDB driver parameters

All parameters correspond to the postgresql JDBC library parameters. See
the
[DataSource Configuration Properties](https://jdbc.postgresql.org/documentation/81/ds-ds.html)
section for detailed parameter documentation.

* **serverName** (required) - database hostname.
* **databaseName** (optional) - database namespace to use; Default *null*.
* **portNumber** (optional) - database listen port; Default *26257*.
* **user** (optional) - database account username; Default *null*.
* **password** (optional) - database account password; Default *null*.
* **connectionpool** (optional) - connection pool implementation; Default
  no connection pool, in other words create a connection per statement execution.
  Allowed values:
    * *hikari* -
      use [HikariCP](https://github.com/brettwooldridge/HikariCP)
* **maxtries** (optional) - number of times to retry retry-able errors; Default *3*.
* **errors** (optional) - expression which specifies how to handle SQL state error codes.
  Expression syntax and behavior is explained in the `error-handlers` topic. Default
  *stop*, in other words exit on any error.
