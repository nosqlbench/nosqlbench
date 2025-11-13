+++
title = "Op Parameters"
description = "Learn how to use operation parameters to configure statement templates"
weight = 50
template = "page.html"

[extra]
quadrant = "tutorials"
topic = "workload-basics"
category = "configuration"
tags = ["workloads", "yaml", "parameters", "configuration"]
+++

## Op Params Defined

Op templates within a YAML can be accessorized with parameters. These are known as _op params_ and
are different from the parameters that you use at the activity level. They apply specifically to a
statement template, and are interpreted by an activity type when the statement template is used to
construct a native statement form.

For example, the op param `ratio` is used when an activity is initialized to construct the op
sequence. In the _cql_ activity type, the op parameter `prepared` is a boolean that can be used to
designated when a CQL statement should be prepared or not.

As with the bindings, a params section can be added at the same level, setting additional parameters
to be used with op templates. Again, this is an example of modifying or otherwise designating a
specific type of op template, but always in a way specific to the activity type. Op params can be
thought of as properties of an operation. As such, params don't really do much on their own,
although they have the same basic map syntax as bindings:

## Op Params Example

```yaml
params:
  ratio: 1
```

As with op template, it is up to each activity type to interpret the provided op params in a useful
way.
