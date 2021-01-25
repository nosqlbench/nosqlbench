# CockroachDB Driver

This is a driver for CockroachDB. It extends the generic JDBC Driver and
inherits its parameters.

### CockroachDB driver parameters

All parameters correspond to the postgresql JDBC library parameters. See
the
[DataSource Configuration Properties](https://jdbc.postgresql.org/documentation/81/ds-ds.html)
section for detailed parameter documentation.

* **serverName** (required) - database hostname
* **portNumber** (optional) - database listen port; defaults to 26257.
* **user** (optional) - database account username; defaults to empty.
* **password** (optional) - database account password; defaults to empty.
* **connectionpool** (optional) - connection pool implementation; defaults
  to no connection pool. Allowed values:
    * *hikari* -
      use [HikariCP](https://github.com/brettwooldridge/HikariCP)
