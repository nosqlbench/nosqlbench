# cqlverify activity type

This activity type allows you to read values from a database and compare them to
the generated values that were expected to be written, row-by-row, producing a
comparative result between the two.

The verification options include:

1. Each row contains the right fields, according to the reference data.
2. Each row contains only the fields specified in the reference data.
3. Each value of each row, by name, is equal to the referenced data,
   according to the Java equals implementation for the object type
   specified in that field's metadata.

The data bindings are used to generate the expected values that would be used
for an upsert. Each row is verified according to these values, and any
discrepancy is treated as an error that can be counted, logged, etc.

### Using cqlverify

The cqlverify activity type is built on top of the cql activity type. As such,
it has all of the same capabilities and options, and then some. See the cql
activity type documentation for the usual details. This doc page only covers how
the cqlverify activity extends it.

The differences between the cql and cqlverify activity types are mostly in how
how you configure for verifiable data and error handling.

##### Writing verifiable data

The cqlverify activity type does not retain logged data for verification. Still,
it is able to compare data as if it had a separate data set to compare to. This
is possibly only because the data generation facilities used by ebcql (and
engineblock) provide realistic and voluminous synthetic data that can be
recalled from a recipe and accessed dynamically.

That means, however, that you must avoid using the non-stable data mapping
functions when writing data. The rule of thumb is to avoid using any data
mapping functions containing the word "Random", as these are the ones that have
historically used internal RNG state. Instead, swap in their replacements that
start with "Hashed". There is a hashed equivalent to all of the original random
functions. The rng-based functions will be deprecated in a future release.

In a typical cql activity, you are allowed to name the bindings however you
like, so long as the binding names match the anchor names in your statement
template. Because we need to match reference field data to actual row data
pair-wise by field name, there is a more strict requirement for cqlverify
activities. The binding names themselves are now required to match the field
names that they are expected to be compared to.

The simplest way to do this is to follow this recipe:

1. Make the binding names the same as the field names that you use in
   in your write statements.
2. When you configure your read statement for the cqlverify activity,
   simply include the same bindings as-is, using the partition and
   clustering fields in the appropriate where clauses.

*note*: It used to be an error to have bindings names in excess of what anchor
names would match. Now, it is only an error if an anchor is not qualified with
a matching binding name. This allows you to simply copy your bindings as-is
directly from your write statement with no issues.

### Configuring the verification Reader

A cqlverify activity is almost exactly like any other cql activity. However, you
configure a single read statement to access the row data you want to verify. The
bindings for the read statement should include the data mappings that you have
for the write statement. That's pretty much all you have to do.

The names of the bindings and the values they produce are considered, depending
on the *compare* setting explained below. This means that you need to make sure
that the bindings that are provided for the statement are exactly the same as
you expect the row structure, irrespective of field order. For some statements
which use the same value in more than one place, you must name these uniquely
as well.

If more than one statement is active for a cqlverify activity, then an error is
thrown. This may change in the future, but for now it is a requirement.

### Handling Verification Errors

The cqlverify activity extends on the error handling stack mechanism that is
used by the cql activity type, by introducing a new error category:
*unverified*. The default configuration for this error category is

    unverified=stop

However, the usual options, including "stop", "warn", "retry", "histogram",
"count", and "ignore" are also allowed.

Care should be taken to set the other error handling categories to be strict
enough to avoid false negatives in testing. The verification on a row can only
be done if the row is actually read first. If you set the error handler stack to
only count real errors, for example, then you will be preempting the read
verifier. Therefore, there is a default setting for the cqlverify activity for
the catch-all error handler parameter *errors*.

This means that the default error handling behavior will cause an exception to
be thrown and the client will exit by default. If you wish for something less
dramatic, then set it to

    errors=...,unverified->count

or

    errors=...,unverified->warn

##### rows to verify

Currently, every read operation in a cqlverify activity must have a single row
in the result set. If there is no row, then the row fails validation. The same
happens if there is more than one row.

A future release may allow for paged reads for quicker verification.

### Example activity definitions

Write 100K cycles of telemetry data

    ... run driver=cql alias=writesome workload=cql-iot tags=group:write cycles=100000 host=...

Verify the the same 100K cycles of telemetry data

    ... run driver=cqlverify alias=verify workload=cql-iot tags=group:verify cycles=100000 host=...

To see how these examples work, consult the telemetry.yaml file in the nosqlbench.jar.

### CQLVerify ActivityType Parameters

(in addition to those provided by the cql activity type)

- **compare** - what to verify. Valid values are "reffields",
  "rowfields", "fields", "values", or "all"
   (default: all)
  - rowfields - Verify that fields in the row, by name, are
    not in excess of what is provided in the reference data.
  - reffields - Verify that fields in the row, by name, are
    present for all all of those provided in the reference data.
  - fields - A synonym for rowfields AND reffields
    (full set equivalence)
  - values - Verify that all the pair-wise fields have equal
    values, according to the type-specific equals method for
    the data type identified in the row metadata by field name.
  - all - A synonym for fields AND values

### CQLVerify Statement Parameters

- **verify-fields** - an optional modifier of fields to verify for a statement.
  If this parameter is not provided, then it is presumed to be '*' by default.
  This is a string which consists of comma-separate values. If the value
  is '*', then all the bindings that are visible for the statement will be
  used as expected values.
  If it is a word that starts with '-', like '-field2', then the name after the
  dash is removed from the list of fields to verify.
  If it is a word that starts with a '+', like '+field3', or a simple word,
  then the field is added to the list of fields to verify.
  This parameter is useful if you have a set of default bindings and want
  to specify which subset of them of them will be used just for this statement.

  If any of the added fields is in the form "f->b", then it is taken as a mapping
  from the field name _f_ in the schema to a binding _b_.

### Metrics

The cqlverify activity type adds some verification-specific metrics:

- alias.verifiedrows - A counter for how many rows passed verification
- alias.unverifiedrows - A counter for how many rows failed verification
- alias.verifiedvalues - A counter for how many field values were verified
- alias.unverifiedvalues - A counter for how many field values were unverified

