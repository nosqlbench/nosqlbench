# Op Templates

The rules around op templates deserve a separate section, given that
there are many techniques that a user can choose from.

The valid elements of the raw workload form are explained below, using
YAML and JSON5 as a schematic language. This guide is not meant to be very
explanatory for new users, but it can serve as a handy reference about how
workloads can be structured.

Any bundled workload loader should test all of these fenced code blocks
and confirm that the data structures are logically equivalent, using any
json5 blocks as a trigger to compare against the prior block.
**This document is a testable specification.**

While some of the examples below appear to be demonstrating basic
cross-format encoding, there is more going on. This document captures a
set of basic sanity rules for all raw workload data, as well as visual
examples and narratives to assist users and maintainers. In short, if you
don't see what you want to do here, it is probably not valid in the
format, and if you know that to be false, then this document needs to be
updated with more details!

---

## Description

**zero or one `description` fields:**

The first line of the description represents the summary of the
description in summary views. Otherwise, the whole value is used.

```yaml
description: |
    summary of this workload
    and more details
```

==

```json5
{
    "description": "summary of this workload\nand more details\n"
}
```

---

## Scenarios

**zero or one `scenarios` fields, containing one of the following forms**

The way that you create macro-level workloads from individual stages is
called *named scenarios* in NB. These are basically command line templates
which can be invoked automatically by calling their name out on your
command line. More details on their usage are in the workload construction
guide. We're focused merely on the structural rules here.

```yaml
# As named scenarios with a single un-named step
scenarios:
    default: run driver=diag cycles=10
```

```json5
{
    "scenarios": {
        "default": "run driver=diag cycles=10"
    }
}
```

OR

```yaml
# As named scenarios with named steps
scenarios:
    default:
        step1: run alias=first driver=diag cycles=10
        step2: run alias=second driver=diag cycles=10
```

```json5
{
    "scenarios": {
        "default": {
            "step1": "run alias=first driver=diag cycles=10",
            "step2": "run alias=second driver=diag cycles=10"
        }
    }
}
```

OR

```yaml
# As named scenarios with a list of un-named steps
scenarios:
    default:
        - run alias=first driver=diag cycles=10
        - run alias=second driver=diag cycles=10
```

```json5
{
    "scenarios": {
        "default": [
            "run alias=first driver=diag cycles=10",
            "run alias=second driver=diag cycles=10"
        ]
    }
}
```

---

## Bindings

**zero or one `bindings` fields, containing a map of named bindings
recipes**

Bindings are the functions which synthesize data for your operations. They
are specified in recipes which are just function chains from the provided
libraries.

```yaml
bindings:
    cycle: Identity();
    name: NumberNameToString();
```

```json5
{
    "bindings": {
        "cycle": "Identity();",
        "name": "NumberNameToString();"
    }
}
```

---

## Params

**zero of one `params` fields, containing a map of parameter names to
values**

Params are modifiers to your operations. They specify important details
which are not part of the operation's command or payload, like consistency
level, or timeout settings.

```yaml
params:
    param1: pvalue1
    param2: pvalue2
```

```json5
{
    "params": {
        "param1": "pvalue1",
        "param2": "pvalue2"
    }
}
```

---

## Tags

**zero or one `tags` fields, containing a map of tag names and values**

Tags are how you mark your operations for special inclusion into tests.
They are basically naming metadata that lets you filter what type of
operations you actually use. Further details on tags are in the workload
construction guide.

```yaml
tags:
    phase: main
```

```json5
{
    "tags": {
        "phase": "main"
    }
}
```

---

## Op Templates

The representation of an operation in the workload definition is the most
flexible as well as the most potentially confusion. The reasons for this
are explained in the README for this module. Thus, it is useful to be
detail oriented in these examples.

An op template, as expressed by the user, is just a recipe for how to
construct an operation at runtime. They are not operations. They are
merely blueprints that the driver uses to create real operations that can
be executed.

This applies at two levels:

1) When the user specifies their op template as part of a workload
   definition.
2) When the loaded workload definition is promoted to a convenient
   OpTemplate type for use by the driver developer.

Just be aware that this term can be used in both ways.

For historic reasons, the field name used for op templates in yaml files
is *statements*, although it will be valid to use any of `statement`,
`statements`, `op`, `ops`, `operation`, or `operations`. This is because
these names are all symbolic and familiar to certain protocols. The
recommended name is `ops` for most cases. Internally, pre-processing will
likely be used to convert them all to simply `ops`.

### a single un-named op template

```yaml
op: select * from bar.table;
```

```json5
{
    "op": "select * from bar.table;"
}
```

### un-named op templates as a list of strings

```yaml
ops:
    - select * from bar.table;
```

```json5
{
    "ops": [
        "select * from bar.table;"
    ]
}
```

### named op templates as a list of maps

```yaml
ops:
    - op1: select * from bar.table;
```

```json5
{
    "ops": [
        {
            "op1": "select * from bar.table;"
        }
    ]
}
```

### named op templates as a map of strings

```yaml
ops:
    op1: select * from bar.table;
```

```json5
{
    "ops": {
        "op1": "select * from bar.table;"
    }
}
```

### named op templates as a map of maps

```yaml
ops:
    op1:
        stmt: select * from bar.table;
```

```json5
{
    "ops": {
        "op1": {
            "stmt": "select * from bar.table;"
        }
    }
}
```

---

## Blocks

Blocks are used to group operations which should be configured or run
together such as during a specific part of a test sequence. Blocks can
contain any of the defined elements above.

### named blocks as a map of property maps

```yaml
blocks:
    block1:
        ops:
            op1: select * from bar.table;
            op2:
                type: batch
                stmt: insert into bar.table (a,b,c) values (1,2,3);
```

```json5
{
    "blocks": {
        "block1": {
            "ops": {
                "op1": "select * from bar.table;",
                "op2": {
                    "type": "batch",
                    "stmt": "insert into bar.table (a,b,c) values (1,2,3);"
                }
            }
        }
    }
}
```

### un-named blocks as a list of property maps

```yaml
blocks:
    - ops:
          op1: select * from bar.table;
          op2:
              type: batch
              stmt: insert into bar.table (a,b,c) values (1,2,3);
```

```json5
{
    "blocks": [
        {
            "ops": {
                "op1": "select * from bar.table;",
                "op2": {
                    "type": "batch",
                    "stmt": "insert into bar.table (a,b,c) values (1,2,3);"
                }
            }
        }
    ]
}
```

---

## Names

All documents, blocks, and ops within a workload can have an assigned
name. When map and list forms are both supported for entries, the map
form provides the name. When list forms are used, an additional field
named `name` can be used.

```yaml
blocks:
    - name: myblock
      op: "test op"

```
```json5
{
    "blocks" : [
        {
            "name": "myblock",
            "op": "test op"
        }
    ]
}
```

# Normalization

