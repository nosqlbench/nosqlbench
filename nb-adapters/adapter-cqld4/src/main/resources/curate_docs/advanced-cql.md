# cql driver - advanced features

This is an addendum to the standard CQL Activity Type docs. For that,
see "cql". Use the features in this guide carefully. They do not come
with as much documentation as they are less used than the main CQL
features.

### ResultSet and Row operators

Within the CQL Activity type, synchronous mode (activities with out the
async= parameter), you have the ability to attach operators to a given
statement such that it will get per-statement handling. These operators
are ways of interrogating the result of an operation, saving values, or
managing other side-effects for specific types of testing.

When enabled for a statement, operators are applied in this order:

1. Activity-level ResultSet operators are applied in specified order.
2. Statement-level ResultSet operators are applied in specified order.
3. Activity-level Row operators are applied in specified order.
4. Statement-level Row operators are applied in specified order.

The result set handling does not go to any extra steps of making
a copy of the data. When a row is read from the result set,
it is consumed from it. Thus, if you want to do anything with
row data, you must apply a row operator as explained below.


### CQL Statement Parameters

- **rsoperators** - If provided as a CQL statement param, then the
    list of operator names that follow, separated by a comma, will
    be used to attach ResultSet operators to the given statement.
    Such operators act on the whole result set of a statement.

- **rowoperators** - If provided as a CQL statement param, then the
    list of operator names that follow, separated by a comma, will
    be used to attache Row operators to the given statement.

## Available ResultSet Operators

- pushvars - Push a copy of the current thread local variables onto
  the thread-local stack. This does nothing with the ResultSet data,
  but is meant to be used for stateful management of these in
  conjunction with the row operators below.
- popvars - Pop the last thread local variable set from the thread-local
  stack into vars, replacing the previous content. This does nothing
  with the ResultSet data.
- clearvars - Clears the contents of the thread local variables. This
  does nothign with the ResultSet data.
- trace - Flags a statement to be traced on the server-side and then
  logs the details of the trace to the trace log file.
- log - Logs basic data to the main log. This is useful to verify that
  operators are loading and triggering as expected.
- assert_singlerow - Throws an exception (ResultSetVerificationException)
  if the ResultSet has more or less than one row.

Examples:

```yaml
 statements:
   - s1: |
      a statement
     rsoperators: pushvars, clearvars
```
## Available Row Operators:

- savevars - Copies the values of the row into the thread-local variables.
- saverows - Copies the rows into a special CQL-only thread local row state.

Examples:

```yaml
  statements:
    - s2: |
       a statement
      rowoperators: saverows
```

## Injecting additional Queries (Future)

It is possible to inject new operations to an activity. However, such operations are _indirect_ to cycles, since they
must be based on the results of other operations. As such, they will not be represented in cycle output or other
advanced features. This is a specific feature for the CQL activity -- implemented internal to the way a CQL cycle is
processed. A future version of NoSQLBench will provide a more uniform way to achieve this result across activity types.
For now, remember that this is a CQL-only capability.

- subquery-statement - Adds additional operations to the current cycle, based
  on the contents of the thread-local row state. The value to this parameter
  is a name of a statement in the current YAML.

local thread based on contents
  of the CQL-only thread local row state. Each row is consumed from this list,
  and a new operation is added to the current cycle.
- subquery-concurrency - Allow subqueries to execute with concurrency, up to
  the level specified.
  default: 1
