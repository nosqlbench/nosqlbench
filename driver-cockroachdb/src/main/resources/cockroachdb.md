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
* **minretrydelayms** (optional) - minimum time in ms to wait before retry with exponential backoff; Default *200*.
* **errors** (optional) - see `error-handlers` topic for details (`./nb help error-handlers`). Default *stop*.

#### errors parameter

This parameter expects an expression which specifies how to handle exceptions by class name
and SQL state code. Error names are formatted as `<exception-name>_<sql-state>`.

For example, a *org.postgresql.util.PSQLException* with *SQLState=80001* will be formatted `PSQLException_80001`.
To continue on such an error, use `errors=PQLException_80001:warn,count;stop`. To retry any
*java.sql.SQLTransientException* or any *SQLState=80001* and otherwise stop, use
`errors=SQLTransientException.*:warn,count,retry;.*80001:warn,count,retry;stop`.

See scenario implementations in workloads `cockroachdb-basic` and `postgres-basic` for reasonable defaults
of the errors parameter. This is a reasonable default error handler chain:

1. `SQLTransient.*:warn,count,retry` - log, emit metric, and retry on transient errors
([java.sql doc](https://docs.oracle.com/javase/8/docs/api/java/sql/SQLTransientException.html))
2. `.*0800.*:warn,count,retry` - log, emit metric, and retry on "connection exception" class of postgresql driver
SQLState codes ([postgresql java doc](https://www.postgresql.org/docs/9.4/errcodes-appendix.html))
3. `.*40001:count,retry` - emit metric and retry on "serialization error" SQLState code of postgresql driver
([postgresql java doc](https://www.postgresql.org/docs/9.4/errcodes-appendix.html)).
These are common with CockroachDB
([doc](https://www.cockroachlabs.com/docs/stable/error-handling-and-troubleshooting.html#transaction-retry-errors)).
4. `stop` - stop the activity for any other error or if max retries are exceeded


