# NoSQLBench Driver Standards

This is a work in progress...

This is the document to read if you want to know if your NoSQLBench driver is complete.
Within this document, the phrase `conformant` will be taken to mean that a driver or feature
is implemented according to the design intent and standards of the NoSQLBench driver API.

While it may be possible to partially implement a driver for basic use, following the guidelines
in this document will ensure that contributed drivers for NoSQLBench work in a familiar and
reliable way for users from one driver to another.

Over time, the standards in this guide will be programmatically enforced by the NoSQLBench
driver API.

## Terms

- NB Driver - The NoSQLBench level driver, the code that this document
  refers to.
- Native driver - An underlying driver which is provided by a vendor or
  project.

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
  that is known to the native driver should be counted.
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

Until the error handling subsystem is put in place, these types of error
handling levels are suggested:

1. stop
2. warn
3. retry
4. histogram
5. count
6. ignore

This serves as an error handling stack, where the user chooses the entry
point. From the user-selected entry point, all of the remaining actions
are taken until the end if possible.

### stop

If an exception occurs, and the user has selected the `stop` error
handler, then the activity should be stopped. This is triggered by
allowing the NB driver to propagate a runtime exception up the stack.

Since this error handler interrupts flow of an activity, no further error
handling is done.

### warn

If an exception occurs and the user has selected the `warn` error handler,
the the exception should be logged at WARN level.

The next error handler `retry` should also be called.

### retry

If an exception occurs and the user has selected the `retry` error
handler, **AND** the exception represents a type of error which could
reasonably be expected to be non-persistent, then the operation should be
re-submitted after incrementing the tries metric.

Whether or not the operation is retried, the next error handler
`histogram` should also be called.

### histogram

If an exception occurs and the user has selected the `histogram` error
handler,the error should be recorded with the help class
`ExceptionHistoMetrics`. This adds metrics under the `errorhistos` label
under the activity's name.

The next error handler `count` should also be called.

### count

If an exception occurs and the user has selected the `count` error
handler, then the error should be counted with the helper class
`ExceptionCountMetrics`. This adds metrics under the `errorcounts` label
under the activity's name.

The next exception handler `ignore` should also be called, but this is
simply a named 'no-op' which is generally the last fall-through case in a
switch statement.

## Naming Conventions

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

## Named Scenarios

Useful driver implementations should come with one or more examples of a
workloads under the activities directory path. These examples should
employ the "named scenarios" format as described in the main docs. By
including named scenarios in the yaml format, these named scenarios then
become available to users when they look for scenarios to call with the
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

## Usage of the Op Template

The operation which will be executed in a driver should be derivable from
the YAML as provided by a user. Thankfully, NoSQLBench goes to great
lengths to make this easy for both to the user and to the driver
developer. In particular, NB presents the user with a set of formatting
options in the YAML which are highly flexible in terms of syntax and
structure. On the other side, it presents the driver developer with a
service interface which contains all the input from the user as a complete
data structure.

This means that the driver developer needs to make it clear how different
forms of content from the YAML will map into an operation. Fundamentally,
a driver is responsible for mapping the fully realized data structure of
an `op template` into an executable operation by NoSQLBench.

In some protocols or syntaxes, the phrase _the statement_ makes sense, as
it is the normative way to describe what an operation does. This is true,
for example, with CQL. In CQL You have a statement which provides a very
clear indication of what a user is expecting to happen. At a more abstract
level, and more correctly, the content that a user puts into the YAML is a
`statement template`, and more generally within NoSQLBench, and `operation
template`. This is simply called an `op template` going forward, but as a
driver developer, you should know that these are simply different levels
of detail around the same basic idea: an executable operation derived from
a templating format.

Since there are different ways to employ the op template, a few examples
are provided here. As a driver developer, you should make sure that your
primary docs include examples like these for users. Good NB driver docs
will make it clear to users how their op templates map to executable
operations.

### op template: statement form

In this form, the op template is provided as a map, which is also an
element of the statements array. The convention here is that the values
for and _the statement_name_ and _the statement_ are taken as the first
key and value. Otherwise, the special properties `name` and `stmt` are
explicitly recognized.

```text
statements:
  - aname: the syntax of the statement with binding {b1}
    tags:
     tag1: tagvalue1
    params:
     param1: paramvalue1
    freeparamfoo: freevaluebar
    bindings:
     b1: NumberNameToString()
```

### op template: map form

```text
statements:
  - cmd_type:
```


Structural variations and conventions.

## Handling secrets

Reading passwords ...
