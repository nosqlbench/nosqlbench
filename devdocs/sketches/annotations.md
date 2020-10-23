# Annotations

NOTE: Here, annotations are notes that are stored in a metrics system for
review, not _Java Annotations_.

The annotations support in nosqlbench is meant to allow for automatic
 annotation of important timestamps and qualifying details for a
  nosqlbench scenario.

# Annotation Semantics

Annotations are treated generally like events which mark a boundary
 between test scenario states in nosqlbench. Some annotations may be
 provided for a span of time, in which case the events are implied as
  start and stop, or beginning and ending.

Annotations always have at least one timestamp, and up to two
. Annotations with one timestamp mark an instant where an event
 is known to have occurred.

When instrumenting an event for annotation, both positive and negative
 outcomes must be instrumented. That is, if a user is expecting an
  annotation marker for when an activity was started, they should
   instead see an error annotation if there indeed was an error. The
    successful outcome of starting an activity is a different event
     than the failure of it, but they both speak to the outcome of
      trying to start an activity.

# NoSQLBench Event Taxonomy

- cli
  - cli.render
  - cli.execution
  - cli.error
- scenario
  - scenario.start
  - scenario.stop
  - scenario.error
- activity
  - activity.start
  - activity.stop
  - activity.param
  - activity.error
- thread
  - thread.state
  - thread.error
- user
  - note
- extension

## tags

type
: <specific event name>
layer
: (user, cli, scenario, activity, thread, op)
span
: (instant, interval)
status
: (ok,error)

# Annotations Stores

## Grafana Annotations

## Logged Annotations
