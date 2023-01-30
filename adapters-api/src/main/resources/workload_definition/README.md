# Workload Specification

This directory contains the testable specification for workload definitions used by NoSQLBench.

## Op Templates vs Developer API
There are two primary views of workload definitions that we care about:

1. The User View of **op templates**
   1. Op templates are simply the schematic recipes for building an operation.
   2. Op templates are provided by users in YAML or JSON or even directly via runtime API.
   3. Op templates can be provided with optional metadata which serves to label, group or
      otherwise make the individual op templates more manageable.
   4. A variety of forms are supported which are self-evident, but which allow users to have
      some flexibility in how they structure their YAML, JSON, or runtime collections.
2. The Developer View of the ParsedOp API -- All op templates, regardless of the form they are
   provided in, are processed into a normalized internal data structure.
   1. The detailed documentation for the ParsedOp API is in javadoc.

The documentation in this directory serve as a testable specification for all the above. It
shows specific examples of all the valid op template forms in both YAML and JSON, as well as how
the data is normalized to feed developer's view of the ParsedOp API.

If you are a new user, it is recommended that you read the basic docs first before delving into
these specification-level docs too much. The intro docs show normative and simple ways to
specific workloads without worrying too much about all the possible forms.

## Templating Language

When users want to specify a set of operations to perform, they do so with the workload templating
format, which includes document level details, block level details, and op level details.
Specific reserved words like `block` or `ops` are used in tandem with nesting structure to
define all valid workload constructions. Because of this, workload definitions are
essentially data structures comprised of basic collection types and primitive values. Any on-disk
format which can be loaded as such can be a valid source of workload definitions.

- [SpecTest Formatting](spectest_formatting.md) - A primer on the example formats used here
- [Workload Structure](workload_structure.md) - Overall workload structure, keywords, nesting
  features
- [Op Template Basics](op-template-basics.md) - Basic Details of op templating
- [Op Template Variations](op_template_variations.md) - Additional op template variants
  and corner cases
- [Template Variables](template_variables.md) - Textual macros and default values

## ParsedOp API

After a workload template is loaded into an activity, it is presented to the driver in an API which
is suitable for building executable ops in the native driver.

- [ParsedOp API](parsed_op_api.md) - Defines the API which developers see after a workload is fully
  loaded.

## Related Reading

If you want to understand the rest of this document, it is crucial that you have a working knowledge
of the standard YAML format and several examples from the current drivers. You can learn this from
the main documentation which demonstrates step-by-step how to build a workload. Reading further in
this document will be most useful for core NB developers, or advanced users who want to know all
the possible ways of building workloads.

## Op Mapping Stages

The process of loading a workload definition occurs in several discrete steps during a NoSQLBench
session:

1. The workload file is loaded.
2. Template variables from the activity parameters are interposed into the raw contents of the
   file.
3. The file is deserialized from its native form into a raw data structure.
4. The raw data structure is transformed into a normalized data structure according to the Op
   Template normalization rules.
5. Each op template is then denormalized as a self-contained data
   structure, containing all the provided bindings, params, and tags from the upper layers of the
   doc structure.
6. The data is provided to the ParsedOp API for use by the developer.
7. The DriverAdapter is loaded which understands the op fields provided in the op template.
8. The DriverAdapter uses its documented rules to determine which types of native driver operations
   each op template is intended to represent. This is called **Op Mapping**.
9. The DriverAdapter (via the selected Op Mapper) uses the identified types to create dispensers of
   native driver operations. This is called **Op Dispensing**.
10. The op dispensers are arranged into an indexed bank of op sources according to the specified
   ratios and or sequencing strategy. From this point on, NoSQLBench has the ability to
   construct an operation for any given cycle at high speed.

These specifications are focused on steps 2-5. The DriverAdapter focuses on the developer's use of
the ParsedOp API, and as such is documented in javadoc primarily. Some details on the ParsedOp
API are shared here for basic awareness, but developers should look to the javadoc for the full
story.

## Mapping vs Running

It should be noted that the Op Mapping stage, where user intentions are mapped from op templates to
native operations is not something that needs to be done quickly. This occurs at
_initialization_ time. Instead, it is more important to focus on user experience factors, such as
flexibility, obviousness, robustness, correctness, and so on. Thus, priority of design factors in
this part of NB is placed more on clear and purposeful abstractions and less on optimizing for
speed. The clarity and detail which is conveyed by this layer to the driver developer will then
enable them to focus on building fast and correct op dispensers. These dispensers are also
constructed before the workload starts running, but are used at high speed while the workload
is running.
