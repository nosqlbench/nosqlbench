# dsegraph activity type

# warning; These docs are a work in progress
This is an activity type which allows for the execution of workloads
using DSE Graph and the DSE Java Driver.

This activity type is wired synchronously within each client 
thread, however the async API is used in order to expose fine-grain 
metrics about op binding, op submission, and waiting for a result.

## Example activity definitions

Run a dsegraph activity named 'a1', with definitions from activities/graphs.yaml
~~~
... type=dsegraph alias=a1 yaml=graphs
~~~

Run a dsegraph activity defined by graphs.yaml, but with shortcut naming
~~~
... type=dsegraph yaml=graphs
~~~

Only run statement groups which match a tag regex
~~~
... type=dsegraph yaml=graphs tags=group:'ddl.*'
~~~

Run the matching 'dml' statements, with 100 cycles, from [1000..1100)
~~~
... type=dsegraph yaml=graphs tags=group:'dml.*' cycles=1000..11000
~~~
This last example shows that the cycle range is [inclusive..exclusive),
to allow for stacking test intervals. This is standard across all
activity types.

## dsegraph ActivityType Parameters

- **yaml** - The file which holds the schema and statement defs. 
    (no default, required)
~~~
DOCS TBD FOR THIS SECTION
- **cl** - An override to consistency levels for the activity. If
    this option is used, then all consistency levels will be replaced
    by this one for the current activity, and a log line explaining
    the difference with respect to the yaml will be emitted.
    This is not a dynamic parameter. It will only be applied at
    activity start.
~~~~    
- **cbopts** - this is how you customize the cluster settings for
    the client, including policies, compression, etc. This is
    a string of *Java*-like method calls just as you would use them
    in the Cluster.Builder fluent API. They are evaluated inline
    with the default Cluster.Builder options not covered below.
    Example: cbopts=".withCompression(ProtocolOptions.Compression.NONE)"
- **maxtries** - how many times an operation may be attempted
~~~
DOCS TBD FOR THIS SECTION
- **diagnose** - if this is set to true, then any exception for an 
    operation are thrown instead of handled internally. This can 
    be useful for diagnosing exceptions during scenario development.
    In this version of ebdse, this is a shortcut for setting all the
    exception handlers to **stop**.
~~~
- **cycles** - standard, however the cql activity type will default 
    this to however many statements are included in the current 
    activity, after tag filtering, etc.
- **username** - the user to authenticate as. This option requires 
    that one of **password** or **passfile** also be defined.
- **password** - the password to authenticate with. This will be 
    ignored if passfile is also present.
- **passfile** - the file to read the password from. The first 
    line of this file is used as the password.
- **alias** - this is a standard engineblock parameter, however 
    the cql type will use the yaml value also as the alias value 
    when not specified.
- **graphson** - the version of the graphson protocol to use:
    default: 2

## Statement Parameters

- **repeat** - if specified, causes the statement blocks to be
  lexically repeated before being evaluated as statements,
  including enumerated bindings.

## Error Handling

#### Error Handlers

When an error occurs, you can control how it is handled. 

This is the error handler stack:

- **stop** - causes the exception to be thrown to the runtime, forcing a shutdown.
- **warn** - log a warning in the log, with details about the error and associated statement.
- **count** - keep a count in metrics for the exception, under the name 
    exceptions.classname, using the simple class name, of course.
- **retry** - Retry the operation if the number of retries hasn't been 
    used up.
- **ignore** - do nothing, do not even retry or count

They are ordered from the most extreme to the most oblivious starting
at the top.  With the exception of the **stop** handler, the rest of 
them will be applied to an error all the way to the bottom. One way 
to choose the right handler is to say "How serious is this to the test
run or the results of the test if it happens?" In general, it is best 
to be more conservative and choose a more aggressive setting unless you 
are specifically wanting to measure how often a given error happens, 
for example.

#### Error Types

The errors that can be detected are sorted into three categories:
~~~
DOCS TBD FOR THIS SECTION

- **unapplied** - This was a LWT that did not get applied. All operations
    are checked, and a ChangeUnapplied exception is thrown.
    (This is a local exception to make error handling consistent)
    This is a separate category from retryable, because you have to
    have reactive logic to properly submit a valid request when it occurs.
~~~
- **retryable** - NoHostAvailable, Overloaded, WriteTimeout, and 
    ReadTimeout exceptions. These are all exceptions that might
    succeed if tried again with the same payload.
- **realerrors** -  ReadFailure, WriteFailure, SyntaxError, InvalidQuery.
    These represent errors that are likely a persistent issue, and
    will not likely succeed if tried again.

To set the error handling behavior, simply pair these categories up with 
an entry point in the error handler stack. Here is an example, showing
also the defaults that are used if you do not specify otherwise:

    retryable=retry realerror=stop 

## Generic Parameters

*provided by the runtime*
- **targetrate** - The target rate in ops/s
- **linkinput** - if the name of another activity is specified, this activity
    will only go as fast as that one.
- **tags** - optional filter for matching tags in yaml sections (detailed help
    link needed)
- **threads** - the number of client threads driving this activity

## Metrics
- \<alias\>.cycles - (provided by core input) A timer around the whole cycle
- \<alias\>.bind - A timer which tracks the performance of the statement
    binding logic, including the generation of data immediately prior
- \<alias\>.execute - A timer which tracks the performance of op submission
    only. This is the async execution call, broken out as a separate step.
- \<alias\>.result - A timer which tracks the performance of an op result only.
    This is the async get on the future, broken out as a separate step.
- \<alias\>.tries - A histogram of how many tries were required to get a
    completed operation

## YAML Format

The YAML file for a DSE Graph activity has one or more logical yaml documents,
each separted by tree dashes: --- the standard yaml document separator. Each
yaml document may contain a tags section for the purpose of including or 
excluding statements for a given activity: 

~~~ (optional)
tags:
  tagname: value
  ...
~~~
If no tags are provided in a document section, then it will be matched by 
all possible tag filters. Conversely, if no tag filter is applied in 
the activity definition, all tagged documents will match.

Statements can be specified at the top level or within named blocks. When
you have simple needs to just put a few statements into the yaml, the top-level
style will suffice:

~~~
name: statement-top-level-example
statements:
- statement 1
- statement 2
~~~

If you need to represent multiple blocks of statements in the same activity,
you might want to group them into blocks:
~~~
blocks:
- name: statement-block-1
  statements:
  - statement 1
  - statement 2
~~~  

At any level that you can specify statements, you can also specify data bindings:

~~~
statements:
- statement 1
- statement 2
bindings:
 bindto1: foo
 bindto2: bar

blocks:
- name: statement-block-1
  statements:
  - statement 1
  bindings:
    bindto1: foo
~~~

Data bindings specify how values are generated to plug into each operation. More
details on data bindings are available in the activity usage guide.

### Parameter Templating

Double angle brackets may be used to drop parameters into the YAML 
arbitrarily. When the YAML file is loaded, and only then, these parameters
are interpolated from activity parameters like those above. This allows you
to create activity templates that can be customized simply by providing
additional parameters to the activity. There are two forms, 
\<\<some_var_name:default_value\>\> and \<\<some_var_name\>\>. The first
form contains a default value. In any case, if one of these parameters is
encountered and a qualifying value is not found, an error will be thrown.

### YAML Location

The YAML file referenced in the yaml= parameter will be searched for in the following places, in this order:
1. A URL, if it starts with 'http:' or 'https:'
2. The local filesystem, if it exists there
3. The internal classpath and assets in the ebdse jar.

The '.yaml' suffix is not required in the yaml= parameter, however it is
required on the actual file. As well, the logical search path "activities/"
will be used if necessary to locate the file, both on the filesystem and in
the classpath.

This is a basic example below that can be copied as a starting template.

## YAML Example
    ---
    CONTENT TBD
     
