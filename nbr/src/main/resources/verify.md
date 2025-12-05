---
title: "Data Verification"
description: "Doc for verify."
tags:
  - docs
audience: developer
diataxis: reference
component: docsys
topic: architecture
status: live
owner: "@nosqlbench/docs"
generated: false
---

# Data Verification

NoSQLBench has the capability to verify data that is the result of an operation by comparing
it with reference data. The reference data is simply the same data which you might use to generate
data for upsert, modeled with standard bindings. Thus, you can assert the correctness of any (whole
or part of) data within a database for which you have data bindings. For the most common case --
writing and then verifying data, the same bindings are used for both phases.

The verifier is a generalized capability that can be used with any operation which produces
a value. At it's core, the verifier is a scriptable block can receive the result value in the
context parameter `result`, with the type of value determined by the op implementation and settings.


If you specify `verify` as the op type with the cqld4 adapter, then it will do the following:

1. Presume that the op structure is effectively a read operation, meaning it will produce a result
   that can be used for comparison. The operation must have a single row in the result set. If there
   is no row, then the row fails validation. The same happens if there is more than one row.
2. Using the provided bindings, re-generate the data separately which was expected to be in the
   database. This is called _reference data_ herein.
3. Verify that the values returned from the database are the same as the reference data, and throw a
   ResultVerificationError for each mis-matching row.

Alternately, you can add the verify and compare options to any regular CQL statement (Simple, Raw,
Prepared) in order to enable this verification logic.

## Verification Options

These options may be attached to an op template directly as op fields, or they may be passed as
activity parameters. NOTE: passing them as activity parameters will only work if all of the active
operations are compatible with the verify options.

- **verify** - an optional modifier of fields to verify for a statement. If this parameter is not
  provided, then it is presumed to be `*` by default. This is a string which consists of
  comma-separate values. This parameter is useful if you have a set of default bindings and want to
  specify which subset of them will be used just for this statement. Each form modifies the list of
  fields to verify incrementally, and multiple options are allowed:
- `*` : If the value is `*`, then all the bindings that are visible for the statement will be used
  as expected values.
- `-`, `-field2` : If it is a word that starts with `-`, like `-field2`, then the name after the
  dash is removed from the list of fields to verify.
- `+`, `+field3` : If it is a word that starts with a `+`, like `+field3`, or a simple word, then
  the field is added to the list of fields to verify.
- `f->b` : If any of the added fields is in the form `f->b`, then it is taken as a mapping from the
  field name `f` in the schema to a binding `b`.

For example,

```yaml
# example op template
ops:
  op1:
    readit: "select ....TBD"
    bindings:
      a: ..
      b: ..
      c: ..
    verify: "*,-b"
```

means _verify all fields from the bindings except `b`_, using the default validation method.

- **compare** - what to verify, naming structure, values, etc. each of these is additive, and
  multiple can be specified.
    - all (the default) - A synonym for fields AND values
    - fields - A synonym for rowfields AND reffields (full set equivalence)
    - rowfields - Verify that the result field names include every reference field name.
    - reffields - Verify that the reference field names include every result field name.
    - values - Verify that all the pair-wise fields have equal values, according to the
      type-specific `.equals(...)` method for the data type identified in the row metadata by field
      name.

For example,

```yaml
# example op template
ops:
  op1:
    readit: "select ....TBD"
    bindings:
      a: ..
      b: ..
      c: ..
    verify: "*,-b"
    compare: reffields
```

means _ensure reference fields are present in result fields by name but do not throw an error if
there are more result fields present, and do not compare values of same-named fields_.

## Verification Results

### Errors

The data bindings are used to generate the expected values that would be used for an upsert. Each
row is verified according to these values, and any discrepancy is treated as an error that can be
counted, logged, etc. If you want to simply count the occurences instead of fast-failing an activity
when a row is unverified, then you simply need to modify the error handler for that activity: '
errors=unverified=count'.

The default error handling behavior will cause an exception to be thrown and NoSQLBench
will exit by default. If you wish for something less dramatic, then
wire the unverified category to something else:

    errors=...,unverified->count

or

    errors=...,unverified->warn

### Metrics

The cqlverify activity type adds some verification-specific metrics:

- alias.verified_results - A counter for how many results were verified
- alias.unverified_results - A counter for how many results were not verified
- alias.verified_values - A counter for how many field values were verified
- alias.unverified_values - A counter for how many field values were unverified

## Advanced Usage

The verify capability does not retain logged data for verification. Still, it is able to compare
data as if it had a separate physical data set to compare to. This is possible only because
virtdata (the data generation layer of NoSQLBench) can provide realistic views of virtual datasets
on the fly.

### Avoid Random data

That means, however, that you must avoid using the non-stable data mapping functions when writing
data. The rule of thumb is to avoid using any data mapping functions containing the word "Random".
Binding functions with `random` in their name behave differently from others in that they will not
produce stable results. Their initialization vector is external to the function definition, such as
when using the system _random_ functions.

> Some will bristle at this misuse of the terms, but connotatively they work well for most
> other users. Actually, all of the algorithms used by
> virtdata are __NOT__ truly random, and are deterministic in some way. However, some rely on
> an initialization vector which is not self-contained within the function definition. As such,
> these functions are not pure functions in practice and thus may not be relied upon to return the
> same result from session to session. The word _random_ in virtdata binding functions indicates
> that a function is non-determinstic in some cases. As long as you avoid these functions,
> you can rely on stable generated data from session to session.

### Single vs Multiple Results

TBD

### Paging vs Non-Paging verification

TBD

### Example activity definitions

Write 100K cycles of telemetry data

> TBD ... run driver=cql alias=writesome workload=cql-iot tags=group:write cycles=100000 host=...

Verify the the same 100K cycles of telemetry data

> TBD ... run driver=cqlverify alias=verify workload=cql-iot tags=group:verify cycles=100000

To see how these examples work, TBD


