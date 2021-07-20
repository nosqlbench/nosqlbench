# NoSQLBench Driver Standards

This is the document to read if you want to know if your NoSQLBench driver
is complete. Within this document, the phrase `conformant` will be taken
to mean that a driver or feature is implemented according to the design
intent and standards of the NoSQLBench driver API.

While it may be possible to partially implement a driver for basic use,
following the guidelines in this document will ensure that contributed
drivers for NoSQLBench work in a familiar and reliable way for users from
one driver to another.

Over time, the standards in this guide will be programmatically enforced
by the NoSQLBench driver API.

## Terms

- NB Driver - The NoSQLBench level driver, the code that this document
  refers to.
- Native driver - An underlying driver which is provided by a vendor or
  project.

## Op Templates

The core building block of a NoSQLBench driver is the op template. This is
the form of a statement or operation that users add to a yaml or workload
editor to represent a single operation.

It is the driver's responsibility to create a quick-draw version of an
operation. This is done by using the OpTemplate API. Rules for how a
developer maps an op template to an op function are not set in stone, but
here are some guidelines:

1. Pre-compute as much as you can.
2. Store re-usable elements of an operation in thread-safe form and re-use
   it wherever possible.
3. Allow as much to be deferred till cycle time as reasonable, assuming
   you can cache it effectively.

A moderately advanced example of caching objects by name is included in
the pulsar driver.

In contrast to the rules about how you map your op templates to op
functions (and then ops), it is *crucial* tha tyou document the rules for
how the fields of an template are used. The content that users provide in
a YAML file are the substance of an op template. It is very important that
you document what this means for users, specifically in terms of how field
names and values map to a specific operation.

## Op Sequencing

A conformant driver should use the standard method of creating an
operational sequence. This means that a driver simply has to provide a
function to map an OpTemplate to a more ready to use form that is specific
to the low level driver in question.

## Metrics

At a minimum, a conformant driver should provide the following metrics:

- bind (timer) - A timer around the code that prepares an executable form
  of a statement.
- execute (timer) - A timer around the code that submits work to a native
  driver. This is the section of code which enqueues an operation to
  complete, but not the part that waits for a response. If a given driver
  doesn't have the ability to hand off a request to an underlying driver
  asynchronously, then do not include this metric.
- result (timer) - A timer around the code that awaits and processes
  results from a native driver. This timer should be included around all
  operations, successful ones and errors too. The timer should start
  immediately when the operation is submitted to the native ddriver, which
  is immediately after the bind timer above is stopped for non-blocking
  APIs, or immediately before an operation is submitted to the native
  driver API for all others.
- result-success (timer) - A timer around the code that awaits and
  processes results from a native driver. This timer should only be
  updated for successful operations. The same timer values should be used
  as those on the result timer, but they should be applied only in the
  case of no exceptions during the operation's execution.
- errorcounts-... (counters)- Each uniquely named exception or error type
  that is known to the native driver should be counted. This is provided
  for you as a side effect of using the NBErrorHandler API.
- tries (histogram) - The number of tries for a given operation. This
  number is incremented before each execution of a native operation, and
  when the result timer is updated, this value should be updated as well
  (for all operations). This includes errored operations.

## Error Handling

Users often want to control what level of sensitivity their tests have to
errors. Testing requirements vary from the basic "shutdown the test when
any error occurs" to the more advanced "tell me when the error rate
exceeds some threshold", and so on. The essential point here is that
without flexibility in error handling, users may not be able to do
reasonable testing for their requirements, thus configurable error
handling is essential.

A core library, NBErrorHandler is provided as a uniform way to handle
these errors. It is documented separately in this dev guide. If you add
this error handler to your action implementation, users will automatically
get a completely configurable and standard way to decide what happens for
specific errors in their workload.

## Result Validation

TBD

## Diagnostic Mode

TBD

## Naming Conventions

TBD

### Parameter naming

Parameters should be formatted as snake_case by default. Hyphens or camel
case often cause issues when using mixed media such as command lines and
yaml formats. Snake case is a simple common denominator which works across
all these forms with little risk of ambiguity when parsing or documenting
how parameters are set apart from other syntax.

## Documentation

Each activity is required to have a set of markdown documentation in its
resource directory. The name of the driver should also be used as the name
of the documentation for that driver.

Additional documentation can be added beyond this file. However, all
documentation for a given driver must start with the drivers name and a
hyphen.

If a driver wants to include topics, the convention is to mention these
other topics within the driver's main help. Any markdown file which is
included in the resources of a driver module will be viewable by users
with the help command `nb help <name>`. For example, if a driver module
contains `../src/main/resources/mydriver-specials.md`, then a user would
be able to find this help by running `nb help mydriver-specials`.

These sources of documentation can be wired into the main NoSQLBench
documentation system with a set of content descriptors.

## Named Scenarios

Conformant driver implementations should come with one or more examples of
a workload under the activities directory path. Useful driver
implementations should come with one or more examples of a workloads under
the activities directory path. These examples should employ the "named
scenarios" format as described in the main docs. By including named
scenarios in the yaml format, these named scenarios then become available
to users when they look for scenarios to call with the
`--list-scenarios` command.

To include such scenario, simply add a working yaml with a scenarios
section to the root of your module under the
`src/main/resources/activities` directory.

## Included Examples

Useful driver implementations should come with a set of examples under the
examples directory path which demonstrate useful patterns, bindings, or
statement forms.

Users can find these examples in the same way as they can find the named
scenarios above with the only difference being their location. By
convention the directory `src/main/resources/examples` directory is where
these are located.

The format is the same as for named scenarios, because the examples *are*
named scenarios. Users can find these by using the `--include=examples`
option in addition to the `--list-scenarios` command.

## Testing and Docs

Complete driver implementations should also come with a set of examples
under the examples directory path.

Unit testing within the NB code base is necessary in many places, but not
in others. Use your judgement about when to *not* add unit testing, but
default to adding it when it seems subjective. A treatise on when and how
to choose appropriate unit testing won't fit here, but suffice it to say
that you can always ask the project maintainers for help on this if you
need.

Non-trivial code in pull requests without any form of quality checks or
testing will not be merged until or unless the project maintainers are
satisfied that there is little risk of user impact. Experimental features
clearly labeled as such will be given more wiggle room here, but the label
will not be removable unless/until a degree of robustness is proven in
some testing layer.

### Testing Futures

In the future, the integration testing and the docs system are intended to
become part of one whole. Particularly, docs should provide executable
examples which can also be used to explain how NB or drivers work. Until
this is done, use the guidelines above.

## Handling secrets

Reading passwords ...

## Parameter Use

Activity parameters *and* statement parameters must combine in intuitive
ways.

### ActivityType Parameters

The documentation for an activity type should have an explanation of all
the activity parameters that are unique to it. Examples of each of these
should be given. The default values for these parameters should be given.
Further, if there are some common settings that may be useful to users,
these should be included in the examples.

### Statement Parameters

The documentation for an activity type should have an explanation of all
the statement parameters that are unique to it. Examples of each of these
should be given. The default values for these parameters should be given.

### Additive Configuration

If there is a configuration element in the activity type which can be
modified in multiple ways that are not mutually exclusive, each time that
configuration element is modified, it should be done additively. This
means that users should not be surprised when they use multiple parameters
that modify the configuration element with only the last one being
applied. An example of this would be adding a load-balancing policy to a
cql driver and then, separately adding another. The second one should wrap
the first, as this is expected to be additive by nature of the native
driver's API.

### Parameter Conflicts

If it is possible for parameters to conflict with each other in a way that
would provide an invalid configuration when both are applied, or in a way
that the underlying API would not strictly allow, then these conditions
must be detected by the activity type, with an error thrown to the user
explaining the conflict.

### Parameter Diagnostics

Each and every activity parameter that is set on an activity *must* be
logged at DEBUG level with the
pattern `ACTIVITY PARAMETER: <activity alias>` included in the log line,
so that the user may verify applied parameter settings. Further, an
explanation for what this parameter does to the specific activity *should*
be included in a following log line.

Each and every statement parameter that is set on a statement *must* be
logged at DEBUG level with the
pattern `STATEMENT PARAMETER: <statement name>: ` included in the log
line, so that the user may verify applied statement settings. Further, an
explanation for what this parameter does to the specific statement *
should* be included in a following log line.

### Environment Variables

Environment variable may be hoisted into a driver's configuration, but only
using explicit mechanisms. By default, environment variables are not injected into
any NoSQLBench usage context where it is not explicitly enabled by the user.
The mechanism of enabling environment variables is simple indirection, using
a symbolic variable reference where they would normally use a value.

Further, the variable must be explicitly enabled for env interpolation
by the developer, and documented as such. Having variables which often use
`$...` formats for other purposes besides environment variables is a nuisance.
Conversely, not supporting env vars in `$...` values which are historically
enabled for such is also a nuisance.

#### format

such as `myparam=$ENV_VAR_FOO`, where the env var name must follow
this pattern:

1. A `$` literal dollar sign.
2. Any alphabetic or underscore character (`[a-zA-Z_]`)
3. Zero or more trailing characters to include optional dots and digits. (`[a-zA-Z0-9_]*`)

Alternately, the `${...}` form is less strict, and allows any characters which are not `}`.
