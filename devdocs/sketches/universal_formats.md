---
title: "Configuration Points"
description: "Developer note: Configuration Points."
audience: developer
diataxis: explanation
tags:
  - devdocs
component: core
topic: architecture
status: draft
owner: "@nosqlbench/devrel"
generated: false
---

NoSQLBench has lots of configurable parameters. The more it is used, the more ways we add for specifying, templating, and indirecting values in various places.

This is an attempt to clarify what patterns are currently supported, what a more consistent view would look like, and a plan for getting there without disrupting existing usage scenarios.

## Configuration Points

These represent the known places that a user might need to provide configuration or template values:

* CLI options
    * global parameters - options which affect the whole process, including any scenarios or activities within
    * component configurations - composable behaviors such as Annotations, which may support nested config details
* Script parameters - named parameters provided directly to a scenario script
* Scenario command parameters (including Activity parameters) - named parameters which accessorize activity commands or other scenario commands like `waitfor`
* Op templates in YAML - The field values used to construct op templates
* Driver-specific configuration files - Separate configuration files provided for a driver, such as the pulsar `config.properties` file

Each of these provides an place for a user to specify the behavior of NoSQLBench, and each generally has a different type of flexibility needed. However, there is some overlap in these usage scenarios.

## Indirection Methods

* On NB CLI, filepath arguments are handed to NBIO, which supports some indirection
* In NBConfig logic, the `IMPORT{<nbio path>}` format is allowed, which defers directly to NBIO
* In CQL driver options, `passfile:...` is supported
* In NBEnvironment, the `${name}` and `$name` patterns are supported
* In YAML op templates, any field containing `{name}` is presumed to be a dynamic variable
* In YAML op glue, any field containing `{{name}}` is presumed to be a capture point
* In YAML op glue any field containing `[[...]]` or `[...]` has special meaning.
* In YAML template variables, `TEMPLATE(name,value)` is supported
* In HTTP driver URl patterns, `E[[...]]` is taken as a URL-Encoded section.
* In labeling, label names and values depend on parameters or other settings like environment
  variables. There is no presently established convention for these.


All of these are different types of indirection, and should be distinct in some cases. Yet, there is room for simplification.

## Conventions

Where possible, idiomatic conventions should be followed, such as '${...}' for environment
variables. This means that at a minimum, all variable references should be possible to represent
with a consistent syntax regardless of how and where they are made. The syntactical variations
on top of this are merely sugar which can be provided if and where it makes sense.

## Usage Matrix

The configuration points and the indirection methods should be analyzed for compatibility and
fit on a matrix.

* scope of variable
  * Some values (of variable references) are bound to the lifetime of a specific component. For
    example:
    * captured values from an op template are valid for the length of the local op sequence
    * activity parameters apply only within the lifetime of that activity
* time of evaluation
  * Values are evaluated at some specific time, which should be clearly defined and understood
    by the user. Examples:
    * binding parameters are evaluated within the cycle for a given operation
    * dynamic activity parameters can assigned before and re-assigned at any time during an
      activities lifecycle.
    * static activity parameters can be assigned only once and evaluated before that activity is
      started.
    * template values are evaluated when workload templates are loaded and processed.
* method of resolution
  * Each variable has a resolver mechanism behind it which does the evaluation when needed. The
    type of resolver depends on the usage context. Examples:
    * binding variables within an op template are resolved by the virtdata library as a
      collection of fields, organized within a set of bindings.
    * environment variables are resolved anywhere recognized by basic logic that reads the
      system environment on-demand.
* mutability within scope
  * Once a value has been resolved, it should be clear whether or not the value may change
    during the lifecycle of its owning component. (maybe this is a way we can clarify scope).
  * The name of an activity is configurable, but it not changeable during the lifetime of an
    activity.
  * Conjecture: Every value is explicitly tied to some component lifecycle. If the value may
    change during the component lifetime, this is mutable and should only be allowed for very
    specific and clearly explained reasons. Otherwise, all values are presumed to be immutable
    from when its value is resolved immediately before or during the initialization of that
    component, until or after that component goes out of scope.
  * Example of explicit mutability: the _dynamic activity parameter_ `threads` can be changed at
    any time, and the system will synchronously modify the concurrency of an activity. However,
    the _static activity parameter_ `stride` is not presently mutable, as the op scheduling
    logic is based on a fixed LUT at activity initialization time.
* required or not - this is a version of "default value or not". There are a few valid patterns
  for negotiating default values and required values which are worth considering:
  * make optional values have a default as a matter of consistency. Any callers aware of
    this variable do not need to know if the value is defined, as a default is always there at
    a minimum.
  * make values truly optional. This is valid only in the case where semantics of a defined
    value vs an undefined value are meaningful to the user and will not otherwise yield a
    configuration inconsistency or unexpected behavior for the user. An example of this is
    when optional behaviors are added to an op dispenser if specified, but which are treated
    as non-extant otherwise.
  * make a value required, but do not provide a default. This is meaningful when a value
    has no reasonable default but which is required as a key parameter to some user-defined
    action. Cases where there are no reasonable defaults include those which the value has a
    significant impact on the result, and the user may not even be aware that they have
    the option to set it. Another scenario is when a previous default value should change due
    to user impact, but it would lead to surprised if changed unseen.
* transforms or not (as in "Parameter Expansion" from the Bash man page)
  * There are various methods of edifying or verifying some of the above properties in place, or
    for making partial transforms like eliding a leading or trailing pattern. These are very
    useful to cut down on boiler plate elsewhere.
* combined forms in scope
  * In some cases, you want to be able to provide a value which is directly composited from
    other variables in scope. For example, a metrics label that uniquely identifies a study
    could be composited from the target system type, the dataset size, and the underlying data
    model. Being able to specify this in a terse form where it is used can reduce complexity and
    dependent configuration elsewhere. The distinction on this usage pattern is that any
    composited variables that are made this way should be surfaced and visible in exactly the
    same way that other variable are. This is contrary to how current template variables are
    handled, as they are textual substitutions which disappear once applied.
  * Example: You want to create a table name which is representative of the dataset size and
    content, and it needs to be unique across these values in order to avoid collisions in DDL
    and DML between test phase. For example, you could create
    `$tablename=${"table"_${k}_${dataset}}`. In this case, `tablename` becomes a canonically
    defined variable in the same scope. This allows reference `${tablename}` just as if it were
    provided fully by a user outside this scope.
* sticky values in scope
  * In some cases, a user may reference a variable multiple times in a context, but may want to
    provide a default which is resolved once and referenced again later after it has been
    resolved. In these cases, it should be valid to provide an initial instance and default,
    with each subsequent reference having an undefined default. Further, it may be considered
    an error to specify multiple defaults for the same variable in scope.
* nested values
  * Some evaluation scopes may be nested within others.. This raises questions about visibility
    in interior scopes and how values may be composed.
* Literals and escapes in composited values

## Sanitization and Safety

The rules for parsing and marshaling different types should be robust and clearly documented for
users.

## Controlling Indirection

You should be able to turn some methods on or off, like environment variables.

## Proposed Universal Form

- `${{<varname>}}`
- `${{<provider>:<varname>}}`




