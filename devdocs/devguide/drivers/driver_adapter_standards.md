# NoSQLBench Driver Adapter Standards

This document is intended to replace the earlier driver standard guide, as the APIs in the upcoming
release are both more streamlined and prescriptive. If you have built a driver in NoSQLBench before,
you will find that there is not much to it with the latest API updates.

---

This is the document to read if you want to know if your NoSQLBench Driver Adapter is complete.
Within this document, the phrase `conformant` will be taken to mean that a DriverAdapter is
implemented according to the design intent and standards of this NoSQLBench guide and the API that
it describes.

While it may be possible to partially implement a Driver Adapter for basic use, following the
guidelines in this document will ensure that contributed drivers for NoSQLBench work in a familiar
and reliable way for users from one driver to another.

Over time, the standards in this guide will be programmatically enforced by the NoSQLBench Driver
Adapter API.

## Terms

- Driver Adapter - The NoSQLBench level driver adapter, the code that this document refers to.
- Native driver - An underlying driver which is provided by a vendor or project.

## Overview

The NoSQLBench runtime supports the usage of multiple native drivers via a runtime layer called the
Driver Adapter. Each DriverAdapter implementation serves as a bridge between users and a native
driver API. For each operation specified by a user (as an op template), a Driver Adapter does the
following:

1) Examine the op fields and values to determine what type of operation a user intends. This process
   is called *op mapping*. (TODO: link to OpMapper JavaDoc)
2) Construct a dispenser object which can create instances of the specific type of operation
   determined above. This step is called *op synthesis*. (TODO: Link to OpDispenser JavaDoc)

The first (op mapping) is done at initialization time, and is responsible for doing all the
pre-vetting of what users put into their op templates (via yaml, json, or whatever). As a mechanism
that determines user intent, clarity is paramount.

The second (op synthesis) is done for each and every cycle of an activity that is run, and must be
done efficiently to allow NoSQLBench to operate as an effective testing instrument. If the op
mapping done correctly, there is no need for an op dispenser object to try to sort out what kind of
operation the user intended. An op dispenser synthesizes the fields together.

These two steps are closely related although they have very distinct responsibilities. They are
connected directly in the API -- The OpMapper dispenses OpDispensers (which dispense Ops). Although
they are connected in this way, you have *exact* control of when each happens in the lifecycle of an
activity: Everything in an op mapper and it's _apply_ method *and* in the constructors of op
dispensers that it creates is done before an activity starts (before the first cycle of that
activity). Everything after that (in the body of the OpDispenser apply method, for example) occurs
within a cycle of an activity.

## Op Templates

Users create op templates by writing YAML or JSON or data structures via the scripting API. These
are simply nested collections in their normative form, with string-based keys and values as in any
data structure. NoSQLBench recognizes a standard format (TODO: LINK THIS)
which is the same across all driver types that can be used with NoSQLBench.

The Op Templates which are provided by users are normalized by NoSQLBench into a standard
representation that is used within the op mapping and synthesis steps. This representation is
provided by the ParsedCommand and ParsedTemplate APIs. The User-Facing construct is _Op Template_,
while the developer building driver adapters only sees _Parsed Commands_ and a fully-normalized API.

## Effective Op Mapping

### Setting the Stage

Op Mapping has specific inputs and specific outputs. On the input side, an op mapper can see as much
as the user specifies in the op template, and possibly more as provided to the op mapper's
constructor. Op Mappers may need access to the activity's parameters or the activity's space cache.
These can be provided from the DriverAdapter base type when needed.

Assuming you provide the activity params and the space cache to an OpMapper implementation, when
it's `apply(ParsedCommand cmd)` method is called, you have access to a few levels of information:

1. The ParsedCommand -- representing the specific details of an operation to be performed:
    * op field names
    * static field values - literal values or any non-string collection type (map, set, list)
    * dynamic field values - Any type which contains a string template or a single binding
2. Op params, specified outside the layer of the op payload above. These are static fields which
   users can specify outside
3. The Activity params. Sometimes you want to provide an activity-wide default for how a type of
   operations works. When this applies, be sure to favor the op-specific parameters over any
   activity params.
4. The state cache for the DriverAdapter instance, AKA the space cache. (TODO: Link to javadocs
   for this)

Since op mapping logic is responsible for creating the op dispenser, an op mapper must pass along
any state, config, or other runtime details needed to create a native operation. You have control of
this since you design and instantiate op mapper types directly from your DriverAdapter
implementation.

### Distinguishing Op Types

There are multiple methods a driver adapter may use to determine what kind of operation a user
intends. Of these mentioned here, the best guidance is to choose the one that most closely mimics
the semantics and extant APIs for the specific native driver in question:

* Use a type designator field like `type: put` or something similar.
* Infer the op type by which field names are present in the template. (be careful with this one!)
* Model the op exactly like the payload of the native driver, and hand the op data directly to the
  native driver as such (only one "type" of op here at the NB level)

### Show Users How

In any case, the method that you choose to use needs to be clearly documented, unambiguous, and
unaffected by the addition of new op types added to your driver in the future. You should
provide examples of each op type in your (driver adapter) documentation. Ideally, your
documentation is based on testable examples that are kept in the source tree and used for both
unti testing *and* user examples.

## Effective Op Synthesis

1. Pre-compute as much as you can in the constructor of the OpDispenser. These objects are retained
   for the life of an activity.
2. Store re-usable elements of an operation in thread-safe form and re-use it wherever possible.


# Congruent Behavior

In order to ensure fairness and equity in how drivers work across systems and vendors, it is
necessary to standardize on what each driver does with its operations. A compliant driver
adapter will do the following:

1. Provide an Op implementation which can be retried without resynthesis
2. Fully read all the data in every result by default. Deviations from this default can only be
   allowed when users explicitly specify something else, and should be accompanied by a
   documentation or logging level warning that it is not normal behavior for a client.
3. Provide metrics about the quantity of elements read in a result.

# Config Sources

Activites have configuration at various levels:

1. Activity-wide parameters, called _activity params_.
2. (within the workload template, like a YAML doc) doc level params
3. (with a workload template, such as a YAML doc) block level params
4. op level params
5. op template fields

Op template fields (seen by the NB driver developer through the
ParsedCommand API) are properly meant to specify a distinct type of operation
by its defined properties, no less or more. However, users will sometimes
put op params into the op template alongside the op fields. This is *OK*.

*The rule of thumb is to ensure that a named field can only be used as an
op field or an op param but not both.* Each ParsedCommand has access to
all of the layers above, and should be used to extract out the fields
which are properly configuration level data before the fields are used
for op mapping. By using this technique, op fields can be configured from any convenient
level.


# Enhancements

* Configuration params that govern op behavior can be specified at any level, including within the op template itself.
* Binding functions can be expressed as named anchors or inline as direct definitions
* Variable capture syntax in parsed op formats is standardized.
...

# Revamp Below!


## Result Validation

TBD

## Diagnostic Mode

TBD

## Naming Conventions

TBD

### Parameter naming

Parameters should be formatted as snake_case by default. Hyphens or camel case often cause issues
when using mixed media such as command lines and yaml formats. Snake case is a simple common
denominator which works across all these forms with little risk of ambiguity when parsing or
documenting how parameters are set apart from other syntax.

## Documentation

Each activity is required to have a set of markdown documentation in its resource directory. The
name of the driver should also be used as the name of the documentation for that driver.

Additional documentation can be added beyond this file. However, all documentation for a given
driver must start with the drivers name and a hyphen.

If a driver wants to include topics, the convention is to mention these other topics within the
driver's main help. Any markdown file which is included in the resources of a driver module will be
viewable by users with the help command `nb help <name>`. For example, if a driver module
contains `../src/main/resources/mydriver-specials.md`, then a user would be able to find this help
by running `nb help mydriver-specials`.

These sources of documentation can be wired into the main NoSQLBench documentation system with a set
of content descriptors.

## Named Scenarios

Conformant driver implementations should come with one or more examples of a workload under the
activities directory path. Useful driver implementations should come with one or more examples of a
workloads under the activities directory path. These examples should employ the "named scenarios"
format as described in the main docs. By including named scenarios in the yaml format, these named
scenarios then become available to users when they look for scenarios to call with the
`--list-scenarios` command.

To include such scenario, simply add a working yaml with a scenarios section to the root of your
module under the
`src/main/resources/activities` directory.

## Included Examples

Useful driver implementations should come with a set of examples under the examples directory path
which demonstrate useful patterns, bindings, or statement forms.

Users can find these examples in the same way as they can find the named scenarios above with the
only difference being their location. By convention the directory `src/main/resources/examples`
directory is where these are located.

The format is the same as for named scenarios, because the examples *are*
named scenarios. Users can find these by using the `--include=examples`
option in addition to the `--list-scenarios` command.

## Testing and Docs

Complete driver implementations should also come with a set of examples under the examples directory
path.

Unit testing within the NB code base is necessary in many places, but not in others. Use your
judgement about when to *not* add unit testing, but default to adding it when it seems subjective. A
treatise on when and how to choose appropriate unit testing won't fit here, but suffice it to say
that you can always ask the project maintainers for help on this if you need.

Non-trivial code in pull requests without any form of quality checks or testing will not be merged
until or unless the project maintainers are satisfied that there is little risk of user impact.
Experimental features clearly labeled as such will be given more wiggle room here, but the label
will not be removable unless/until a degree of robustness is proven in some testing layer.

### Testing Futures

In the future, the integration testing and the docs system are intended to become part of one whole.
Particularly, docs should provide executable examples which can also be used to explain how NB or
drivers work. Until this is done, use the guidelines above.

## Handling secrets

Reading passwords ...

## Parameter Use

Activity parameters *and* statement parameters must combine in intuitive ways.

### ActivityType Parameters

The documentation for an activity type should have an explanation of all the activity parameters
that are unique to it. Examples of each of these should be given. The default values for these
parameters should be given. Further, if there are some common settings that may be useful to users,
these should be included in the examples.

### Statement Parameters

The documentation for an activity type should have an explanation of all the statement parameters
that are unique to it. Examples of each of these should be given. The default values for these
parameters should be given.

### Additive Configuration

If there is a configuration element in the activity type which can be modified in multiple ways that
are not mutually exclusive, each time that configuration element is modified, it should be done
additively. This means that users should not be surprised when they use multiple parameters that
modify the configuration element with only the last one being applied. An example of this would be
adding a load-balancing policy to a cql driver and then, separately adding another. The second one
should wrap the first, as this is expected to be additive by nature of the native driver's API.

### Parameter Conflicts

If it is possible for parameters to conflict with each other in a way that would provide an invalid
configuration when both are applied, or in a way that the underlying API would not strictly allow,
then these conditions must be detected by the activity type, with an error thrown to the user
explaining the conflict.

### Parameter Diagnostics

Each and every activity parameter that is set on an activity *must* be logged at DEBUG level with
the pattern `ACTIVITY PARAMETER: <activity alias>` included in the log line, so that the user may
verify applied parameter settings. Further, an explanation for what this parameter does to the
specific activity *should*
be included in a following log line.

Each and every statement parameter that is set on a statement *must* be logged at DEBUG level with
the pattern `STATEMENT PARAMETER: <statement name>: ` included in the log line, so that the user may
verify applied statement settings. Further, an explanation for what this parameter does to the
specific statement *
should* be included in a following log line.

### Environment Variables

Environment variable may be hoisted into a driver's configuration, but only using explicit
mechanisms. By default, environment variables are not injected into any NoSQLBench usage context
where it is not explicitly enabled by the user. The mechanism of enabling environment variables is
simple indirection, using a symbolic variable reference where they would normally use a value.

Further, the variable must be explicitly enabled for env interpolation by the developer, and
documented as such. Having variables which often use
`$...` formats for other purposes besides environment variables is a nuisance. Conversely, not
supporting env vars in `$...` values which are historically enabled for such is also a nuisance.

#### format

such as `myparam=$ENV_VAR_FOO`, where the env var name must follow this pattern:

1. A `$` literal dollar sign.
2. Any alphabetic or underscore character (`[a-zA-Z_]`)
3. Zero or more trailing characters to include optional dots and digits. (`[a-zA-Z0-9_]*`)

Alternately, the `${...}` form is less strict, and allows any characters which are not `}`.
