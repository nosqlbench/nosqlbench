# CQL Statements

This guide is a work in progress, thus it is not added to any topic index yet.

This guide is a deep dive on how NoSQLBench works with CQL statements specifically with the DataStax
Java Driver.

## Raw Statement

A raw statement is neither prepared nor parameterized. That is, it is sent as a single string with
no separate values object. The values must be parsed out during query processing.

In the driver, you might make one of these with the various statement builder APIs, or maybe even
directly like this:

    new SimpleStatement(
      "insert into foo.bar (baz) values ('beetle')"
    );

This is the least efficient type of statement as it will always require the statement structure and
the values to be parsed out when the statement is handled on the server side.

## Parameterized Statement

A parameterized statement is one where the statement form and the parameters are provided
separately.

You can create a parameterized SimpleStatement with named parameters like this:

    new SimpleStatement(
      "insert into foo.bar (baz) values(:bazvalue)",
      Map.of("bazvalue","beetle")
    );

This shows the driver conventions for assigning a named parameter anchor in the statement
`:bazvalue`.

The positional form which uses `?` *is not recommended* for use with NoSQLBench. Named anchors allow
a basic cross-checking ability that is done automatically by NoSQLBench. Thus, positional use will
not be covered here.

### non-parameterizable fields

Some elements of CQL syntax, like row-based-access-control principle names are not parameterizable,
yet you may want to template them at the op template level in nosqlbench. This distinction is very
subtle, but important. When dealing with these forms, it is best to avoid using prepared statements,
since each operation will have a different rendered form.

## Prepared Statement

Prepared statements are the fastest way to invoke a CQL operation from the driver as they avoid
reprocessing the query form on the client and server. However, this means that they act as a
statement template which can be combined with statement parameters to yield an executable statement.
Thus, in pracice, all prepared statements are also parameterized statements.

What makes prepared statement faster is that they aren't parsed by the server (or the client) once
they are prepared. Thus, part of the processing required for a raw statement has already been done
and cached with prepared statements.

Putting these together, the taxonomy of CQL statement forms supported by the NoSQLBench CQL driver
are: (TBD))






