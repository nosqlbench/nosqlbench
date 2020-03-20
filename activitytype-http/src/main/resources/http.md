# http activity type

This activity type allows for basic HTTP requests.
As of this release, only GET requests are supported.

## Example activity definitions

Run an http activity named 'http-test', with definitions from activities/http-google.yaml:
~~~
... driver=http workload=http-google
~~~

This last example shows that the cycle range is [inclusive..exclusive),
to allow for stacking test intervals. This is standard across all
activity types.

## stdout ActivityType Parameters

- **host** - The hosts to send requests to. The hosts are selected in
  round-robin fashion.
  (default: localhost)
- **workload** - The workload definition file which holds the schema and statement defs.
  (no default, required)
- **cycles** - standard, however the activity type will default
  this to however many statements are included in the current
  activity, after tag filtering, etc.
  (default: 0)
- **alias** - this is a standard nosqlbench parameter
  (default: derived from the workload name)

## Configuration

This activity type uses the uniform yaml configuration format.
For more details on this format, please refer to the
[Standard YAML Format](http://docs.nosqlbench.io/user-guide/standard_yaml/)

## Configuration Parameters

- **ratio** - If a statement has this param defined, then it determines
  whether or not to automatically add a missing newline for that statement
  only. If this is not defined for a statement, then the activity-level
  parameter takes precedence.
- **seq** - The statement sequencer scheme.
  (default: bucket)

## Statement Format

The statement format for this activity type is a simple string. Tokens between
curly braces are used to refer to binding names, as in the following example:

    statements:
     - "/{path}?{queryparam1}"
