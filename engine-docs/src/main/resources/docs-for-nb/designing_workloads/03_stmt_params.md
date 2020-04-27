---
title: 03 Statement Params
weight: 03

---

# Statement Parameters

Statements within a YAML can be accessorized with parameters. These are known as _statement params_ and are different
than the parameters that you use at the activity level. They apply specifically to a statement template, and are
interpreted by an activity type when the statement template is used to construct a native statement form.

For example, the statement parameter `ratio` is used when an activity is initialized to construct the op sequence. In
the _cql_ activity type, the statement parameter `prepared` is a boolean that can be used to designated when a CQL
statement should be prepared or not.

As with the bindings, a params section can be added at the same level, setting additional parameters to be used with
statements. Again, this is an example of modifying or otherwise creating a specific type of statement, but always in a
way specific to the activity type. Params can be thought of as statement properties. As such, params don't really do
much on their own, although they have the same basic map syntax as bindings:

```yaml
params:
 ratio: 1
```

As with statements, it is up to each activity type to interpret params in a useful way.

