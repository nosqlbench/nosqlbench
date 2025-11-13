+++
title = "Error Handlers"
description = "How to configure error handling in NoSQLBench activities"
weight = 30
template = "page.html"

[extra]
quadrant = "guides"
topic = "workload-design"
category = "configuration"
tags = ["error-handling", "configuration", "resilience"]
+++

NoSQLBench v5 has a standard error handler mechanism which is available to all drivers.

If no error handler is configured for an activity, then the default error handler is used:

    errors=stop

This error handler is a modular and highly configurable error handler with a very basic set of
defaults: If you don't configure it, then any error thrown by an activity will cause it to
stop. This is indicated by the default `errors=stop`.

The default configuration is just a fail-fast default. It is the simplest possible error handler
configuration.

## Handler Verbs

The currently supported handler verbs, like `stop` above, are:

* **ignore** If an error matches this verb in a handler chain, then it will be silently ignored.
  It will not be retried. Although combining ignore with retry will cause it to be silently retried.
* **counter** Count each uniquely named error with a counter metric.
* **histogram** Track the session time of each uniquely named error with a histogram.
* **meter** Meter each uniquely named error with a meter metric.
* **retry** - Mark the error as retryable. If an activity has retries available, the operation will
  be retried. By default, activities which allow ops to be retried will have `maxtries=10`.
* **stop** Allow the error to propagate through the stack to cause the activity to be stopped.
* **timer** Count, Meter, and Track session times of each uniquely named error with a timer metric,
  which combines the three forms above.
* **warn** Log a warning to the log with the error details.
* **code** Assign a specific result code to any matching errors. You can configure this as
  shorthand in a handler list as just a number: `errors=RuntimeException:33,warn`

You can use any of these verbs in any order in a handler list.

## Chains of Handler Lists

The structure of a handler is a list of list. In order to make this easier to discuss in terms of
configuration, the top level is called the _Handler Chain_ and the individual handler lists at each
link in the chain are called _Handler Verbs_.

The first matching set of handler verbs is used. When an exception is thrown, the handler chain
is walked until a matching handler is found. If none are found, then no handlers apply, which is
the same as assigning `ignore`, which does nothing. If you want a default handler list to apply
to otherwise-unmatched exceptions, be sure to add one at the end for anything matching `.*`.

The illustration below shows the handler chain structure for the handler configuration shown.

     errors=Invalid.*:ignore;Runtime.*:warn,histogram;counter,42

                         |-----------|-----------|-----------|
     handler chain ==>   | 1         | 2         | 3         |
                         |===========|===========|===========|
     error patterns ==>  | Invalid.* | Runtime.* | .*        |
                         |===========| Array.*   |===========|
     handler verbs ==>   | ignore    |===========| counter   |
                         |-----------| warn      | 42        |
                                     | histogram |-----------|
                                     |-----------|

In this example, there are three handler configuration. When an error is thrown by an activity, the
handler chain is called. When a matching handler is found by matching any of the error patterns
against the exception name, that column (the handler list) is selected, and each handler in that
list is applied in order to the error.

Specifically, the Java class name of the exception type is matched against the error patterns going
left to right. The first one that matches selects the error handler list to use.

## Error Handler Configuration

The default setting of `errors=stop` uses a shorthand form for specifying error handlers. The
parameter name `errors` will become the universal activity parameter for configuring error handling
going forward.

`errors=stop` is shorthand for `errors=.*:stop`. This is simply a single handler list which has the
default wildcard error matcher.

A handler definition is thus comprised of the error matching patterns and the error handling verbs
which should be applied when an error matches the patterns. If the error matching patterns are not
provided, then the default wildcard pattern and delimiter `.*:`is automatically prepended.

### Error Pattern Formats

An error pattern is simply a regular expression, although the characters are limited intentionally
to a subset: `[a-zA-Z0-9.-_*+]`. Multiple patterns may be used, like `Missing.*,RuntimeEx.*`.

More customizable ways to map an error to a particular handler list may be provided if/when needed.

### Handler Verb Formats

A specific handler list is defined as a set of handler verbs separated by commas. Alternately,
handler verbs may be blocks of JSON or other standard NoSQLBench encoding formats, as long as they
are protected by quotes:

    # basic verb-only form
    counter,warn

    # using JSON
    "{\"handler\"=\"counter\"},{\"handler\"=\"warn\"}"

    # using simplified params form
    "handler=counter,handler=warn,handler=code code=42"

This shows that handler verbs are really just shorthand for more canonical object definitions which
have their own properties. The handler property is the one that select which handler implementation
to use. Each handler implementation may have its own options. Those will be documented as they are
added.

## Building Handler Chains

To construct a handler entry, simply concatenate the error pattern to the verbs using a colon.

To have multiple handler entries, concatenate them in the order of your choosing with semicolons.

## Examples

Count (in metrics) all occurrences of exceptions named Missing.* (anything matching the regex), but
count and warn about anything matching the exception nameRuntimeEx.*. Ignore everything else.

    errors='Missing.*:counter;RuntimeEx.*:counter,warn;.*:ignore'

Retry all operations which experience an IOException:

    errors='IOException:retry'
