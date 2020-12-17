# Annotations

NOTE: Here, annotations are notes that are stored in a metrics system for
review, not _Java Annotations_.

The annotations support in nosqlbench is meant to allow for automatic
 grafanaAnnotation of important timestamps and qualifying details for a
  nosqlbench scenario.

# Annotation Semantics

Annotations are treated generally like events which mark a boundary
 between test scenario states in nosqlbench. Some annotations may be
 provided for a span of time, in which case the events are implied as
  start and stop, or beginning and ending.

Annotations always have at least one timestamp, and up to two
. Annotations with one timestamp mark an instant where an event
 is known to have occurred.

When instrumenting an event for grafanaAnnotation, both positive and negative
 outcomes must be instrumented. That is, if a user is expecting an
  grafanaAnnotation marker for when an activity was started, they should
   instead see an error grafanaAnnotation if there indeed was an error. The
    successful outcome of starting an activity is a different event
     than the failure of it, but they both speak to the outcome of
      trying to start an activity.

# NoSQLBench Annotation Level

Each annotation comes from a particular level of execution with
 NoSQLBench. Starting from the top, each layer is nested within
 the last. The conceptual view of this would appear as:

              +--------+
              |   op   |
            +------------+ 
            |   motor    |
          +-----------------+ 
          |    activity     |
        +---------------------+ 
        |     scripting       |
      +-------------------------+   +---------------+
      |       scenario          |   |  application  |
    +-------------------------------------------------+
    |               CLI ( Command Line Interface )    |
    +-------------------------------------------------+
    
    
That is, every op happens within a thread motor, every thread motor
happens within an activity, and so on. 
 
- cli
  - cli.render - When the CLI renders a scenario script
  - cli.execution - When the CLI executes a scenario
  - cli.error - When there is an error at the CLI level
- scenario
  - scenario.params - When a scenario is configured with parameters
  - scenario.start - When a scenario is started
  - scenario.stop - When a scenario is stopped
  - scenario.error - When a scenario throws an error
- scripting
  - extensions - When an extension service object is created
- activity
  - activity.params - When params are initially set or changed
  - activity.start - Immediately before an activity is started
  - activity.stop - When an activity is stopped
  - activity.error - When an activity throws an error
- motor
  - thread.state - When a motor thread changes state 
  - thread.error - When a motor thread throws an error
- op
  -- There are no op-level events at this time
- application
  -- There are no application-level events at this time

## tags

These standard tags should be added to every annotation emitted by
 NoSQLBench:
 
**appname**: "nosqlbench"
**layer**: one of the core layers as above
**event**: The name of the event within the layer as shown above


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
