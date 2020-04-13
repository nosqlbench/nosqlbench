# stdout activity type

This is an activity type which allows for the generation of data
into to stdout or a file. It reads the standard nosqlbench YAML
format. It can read YAML activity files for any activity type
that uses the curly brace token form in statements.

## Example activity definitions

Run a stdout activity named 'stdout-test', with definitions from activities/stdout-test.yaml
~~~
... driver=stdout workload=stdout-test
~~~

Only run statement groups which match a tag regex
~~~
... driver=stdout workload=stdout-test tags=group:'ddl.*'
~~~

Run the matching 'dml' statements, with 100 cycles, from [1000..1100)
~~~
... driver=stdout workload=stdout-test tags=group:'dml.*' cycles=1000..11000 filename=test.csv
~~~

This last example shows that the cycle range is [inclusive..exclusive),
to allow for stacking test intervals. This is standard across all
activity types.

## stdout ActivityType Parameters

- **filename** - this is the name of the output file
    (defaults to "stdout", which actually writes to stdout, not the filesystem)
- **newline** - whether to automatically add a missing newline to the end
   of any statements.
   default: true
- **format** - which format to use. If provided, the format will override any statement formats provided by the YAML.
  valid values are (csv, readout, json, inlinejson, assignments, and diag)
  - When 'format=diag', then the internal construction logic for the binding is logged in detail and nosqlbench exits.
    This is useful for detailed diagnostics when you run into trouble, but not generally otherwise. This provides
    details that you may include in a bug report if you think there is a bindings bug.

## Configuration

This activity type uses the uniform yaml configuration format.
For more details on this format, please refer to the
[Standard YAML Format](http://docs.nosqlbench.io/user-guide/standard_yaml/)

## Configuration Parameters

- **newline** - If a statement has this param defined, then it determines
  whether or not to automatically add a missing newline for that statement
  only. If this is not defined for a statement, then the activity-level
  parameter takes precedence.

## Statement Format

The statement format for this activity type is a simple string. Tokens between
curly braces are used to refer to binding names, as in the following example:

    statements:
     - "It is {minutes} past {hour}."

If you want to suppress the trailing newline that is automatically added, then
you must either pass `newline=false` as an activity param, or specify it
in the statement params in your config as in:

    params:
     newline: false

### Auto-generated statements

If no statement is provided, then the defined binding names are used as-is
to create a CSV-style line format. The values are concatenated with
comma delimiters, so a set of bindings like this:

    bindings:
     one: Identity()
     two: NumberNameToString()

would create an automatic string template like this:

    statements:
     - "{one},{two}\n"

The auto-generation behavior is forced when the format parameter is supplied.
