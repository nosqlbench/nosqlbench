# Annotations

NOTE: Here, annotations are notes that are stored in a metrics system for
review, not _Java Annotations_.

The annotations support in nosqlbench is meant to allow for automatic
annotation of important timestamps and qualifying details for a nosqlbench
scenario.

# Annotation Semantics

Annotations are treated generally like events which mark a boundary
between test scenario states in nosqlbench.

Annotations always describe at least one instant in time, and possibly
two. When two instants are connected together with an annotation, then the
annotation describes the lifetime that is bounded by two events:
the beginning and the end. Annotations which describe an instant in time
are tagged as "span: instant", and those which are connected with two
different instants are tagged as "span: interval".

When instrumenting an event for annotation, both positive and negative
outcomes must be instrumented. That is, if a user is expecting an
annotation marker for when an activity was started, they should instead
see an error annotation if there indeed was an error. The successful
outcome of starting an activity is a different event than the failure of
it, but they both speak to the outcome of trying to start an activity.

# NoSQLBench Annotation Level

Each annotation comes from a particular level of execution with
NoSQLBench. Starting from the top, each layer is nested within the last.
The conceptual view of this would appear as:

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
- op -- There are no op-level events at this time
- application -- There are no application-level events at this time

## Annotation Contents

### labels

Labels identify the event for the purposes of filtering in views
and analysis. These standard labels should be added to every annotation emitted by
NoSQLBench:

appname
: "nosqlbench"

session
: the NoSQLBench session ID

layer
: one of the core layers as above

event
: The name of the event within the layer as shown above

layer
: (user, cli, scenario, activity, thread, op)

span
: (instant, interval)

status
: (ok,error)

### details

Details is a map of properties which are canonical for the type of
annotation being recorded, but which are not used for naming or lookup.
At least one detail property should be added for each annotation. You could
say that the labels identify the canonical type of annotation, while the details
provide contextual information that makes the event described by the annotation
unique within that type.

All labels are considered details, and are packaged and represented for display
anywhere details would be.

## Annotation Implementations

NoSQLBench comes with two built-in annotation implementations,
which are configured automatically. By default, the logging
annotator is enabled.

If --docker-metrics is used, then the grafana annotator is enabled.
In this case, if needed, default admin credentials will be used
to create an API key, which will be cached on the local filesystem.
