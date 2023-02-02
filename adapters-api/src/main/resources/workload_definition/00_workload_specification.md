# Workload Specification

This directory contains the testable specification for workload definitions used by NoSQLBench.
All the content blocks in this section have been validated with the latest NoSQLBench build.

Usually, users will not need to delve too deeply into this section. It is useful as a detailed
guide for contributors and driver developers. If you are using a driver which leaves you
wondering what a good op template example looks like, then the driver needs better examples in
its documentation!

# Synopsis

There are two primary views of workload definitions that we care about:

1. The User View of **op templates**
   1. Op templates are simply the schematic recipes for building an operation once you know the
      cycle it is for.
   2. Op templates are provided by users in YAML or JSON or even directly via runtime API. This
      is called a workload template, which contains op templates.
   3. Op templates can be provided with optional metadata which serve to label, group,
      parameterize or otherwise make the individual op templates more manageable.
   4. A variety of forms are supported which are self-evident, but which allow users to have
      some flexibility in how they structure their YAML, JSON, or runtime collections. **This
      specification is about how these various forms are allowed, and how they relate to a
      fully-qualified and de-normalized op template view.
2. The Developer View of the ParsedOp API. This is the view of an op template which presents the
   developer with a very high-level toolkit for building op synthesis functions.

# Details

The documentation in this directory serve as a testable specification for all the above. It
shows specific examples of all the valid op template forms in both YAML and JSON, as well as how
the data is normalized to feed developer's view of the ParsedOp API.

## Related Reading

If you want to understand the rest of this document, it is crucial that you have a working knowledge
of the standard YAML format and several examples from the current drivers. You can learn this from
the main documentation which demonstrates step-by-step how to build a workload. Reading further in
this document will be most useful for core NB developers, or advanced users who want to know all
the possible ways of building workloads.

