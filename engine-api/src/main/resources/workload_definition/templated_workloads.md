# Workload Templates

The valid elements of the raw workload form are explained below, using YAML and JSON5 as a schematic
language. This guide is not meant to be very explanatory for new users, but it can serve as a handy
reference about how workloads can be structured.

Any bundled workload loader should test all of these fenced code blocks and confirm that the data
structures are logically equivalent, using any json5 blocks as a trigger to compare against the
prior block.
**This document is a testable specification.**

While some examples below appear to be demonstrating basic cross-format encoding, there is more
going on. This document captures a set of basic sanity rules for all raw workload data, as well as
visual examples and narratives to assist users and maintainers. In short, if you don't see what you
want to do here, it is probably not valid in the format, and if you know that to be false, then this
document needs to be updated with more details!

# Keywords

The following words have special meaning in templated workloads:

- name - names things
- desc, description - describes the current element. First line is summary.
- scenarios - describes named command sequences for automated workflows
- bindings - defines procedural generation functions
- params - decorates operations with special configurations
- tags - describes elements for filtering and grouping
- op, ops, operations statement, statements - defines op templates
- blocks - groups any or all elements

# Layout of Examples

The specifications and examples below follow this pattern:

1. Some or part of a templated workload in yaml format.
2. The JSON equivalent as it would be loaded. This is cross-checked against the result of parsing
   the yaml into data.
3. The Workload API view of the same data rendered as a JSON data structure. This is cross-checked
   against the workload API's rendering of the loaded data.

To be matched by the testing layer, you must prefix each section with a format marker with emphasis,
like this:

*format:*

```text
body of example
```

Further, to match the pattern above, these must occur in sequences like the following, with no other
intervening content:

*yaml:*

```yaml
# some yaml here
```

*json:*

```
[]
```

*ops:*

```
[]
```

---

## Description

**zero or one `description` fields:**

The first line of the description represents the summary of the description in summary views.
Otherwise, the whole value is used.

*yaml:*

```yaml
description: |
  summary of this workload
  and more details
```

*json:*

```json5
{
  "description": "summary of this workload\nand more details\n"
}
```

*ops:*

```json5
[]
```

---

## Scenarios

**zero or one `scenarios` fields, containing one of the following forms**

The way that you create macro-level workloads from individual stages is called *named scenarios* in
NB. These are basically command line templates which can be invoked automatically by calling their
name out on your command line. More details on their usage are in the workload construction guide.
We're focused merely on the structural rules here.

### single un-named step

*yaml:*

```yaml
scenarios:
  default: run driver=diag cycles=10
```

*json:*

```json5
{
  "scenarios": {
    "default": "run driver=diag cycles=10"
  }
}
```

*ops:*

```json5
[]
```

### multiple named steps

*yaml:*

```yaml
scenarios:
  default:
    step1: run alias=first driver=diag cycles=10
    step2: run alias=second driver=diag cycles=10
```

*json:*

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

*ops:*

```json5
[]
```

### list of un-named steps

*yaml:*

```yaml
scenarios:
  default:
    - run alias=first driver=diag cycles=10
    - run alias=second driver=diag cycles=10
```

*json:*

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

*ops:*

```json5
[]
```

### silent locked step parameters

For scenario steps which should not be overridable by user parameters on the command line, a double
equals is used to lock the values for a given step without informing the user that their provided
value was ignored. This can be useful in cases where there are multiple steps and some parameters
should only be changeable for some steps.

*yaml:*

```yaml
# The user is not allowed to change the value for the alias parameter, and attempting to do so
# will cause an error to be thrown and the scenario halted.
scenarios:
  default: run alias==first driver=diag cycles=10
```

*json:*

```json5
{
  "scenarios": {
    "default": "run alias==first driver=diag cycles=10"
  }
}
```

*ops:*

```json5
[]
```

### verbose locked step parameters

For scenario steps which should not be overridable by user parameters on the command line, a triple
equals is used to indicate that changing these parameters is not allowed. If a user tries to
override a verbose locked parameter, an error is thrown and the scenario is not allowed to run. This
can be useful when you want to clearly indicate that a parameter must remain as it is.

*yaml:*

```yaml
# The user is not allowed to change the value for the alias parameter, and attempting to do so
# will cause an error to be thrown and the scenario halted.
scenarios:
  default: run alias===first driver=diag cycles=10
```

*json:*

```json5
{
  "scenarios": {
    "default": "run alias===first driver=diag cycles=10"
  }
}
```

*ops:*

```json5
[]
```

---

## Bindings

**zero or one `bindings` fields, containing a map of named bindings recipes**

Bindings are the functions which synthesize data for your operations. They are specified in recipes
which are just function chains from the provided libraries.

*yaml:*

```yaml
bindings:
  cycle: Identity();
  name: NumberNameToString();
```

*json:*

```json5
{
  "bindings": {
    "cycle": "Identity();",
    "name": "NumberNameToString();"
  }
}
```

*ops:*

```json5
[]
```

---

## Params

**zero of one `params` fields, containing a map of parameter names to values**

Params are modifiers to your operations. They specify important details which are not part of the
operation's command or payload, like consistency level, or timeout settings.

*yaml:*

```yaml
params:
  param1: pvalue1
  param2: pvalue2
```

*json:*

```json5
{
  "params": {
    "param1": "pvalue1",
    "param2": "pvalue2"
  }
}
```

*ops:*

```json5
[]
```

---

## Tags

**zero or one `tags` fields, containing a map of tag names and values**

Tags are how you mark your operations for special inclusion into tests. They are basically naming
metadata that lets you filter what type of operations you actually use. Further details on tags are
in the workload construction guide.

*yaml:*

```yaml
tags:
  phase: main
```

*json:*

```json5
{
  "tags": {
    "phase": "main"
  }
}
```

*ops:*

```json5
[]
```

---

## Blocks

Blocks are used to group operations which should be configured or run together such as during a
specific part of a test sequence. Blocks can contain any of the defined elements above. Blocks are
most useful for organizing a set of operations together, particularly when you put a tag on a block
to filter by.

Blocks are not recursive. You may not put a block inside another block.

You can think of a block as a subgroup of a document, where all the fields that are described above
can be specified.

### named blocks as a map of property maps

*yaml:*

```yaml
blocks:
  namedblock1:
    ops:
      op1: select * from bar.table;
      op2:
        type: batch
        stmt: insert into bar.table (a,b,c) values (1,2,3);
```

*json:*

```json5
{
  "blocks": {
    "namedblock1": {
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

*ops:*

```json5
[
  {
    "name": "namedblock1--op1",
    "op": {
      "stmt": "select * from bar.table;"
    },
    "tags": {
      "name": "namedblock1--op1",
      "block": "namedblock1"
    }
  },
  {
    "name": "namedblock1--op2",
    "op": {
      "stmt": "insert into bar.table (a,b,c) values (1,2,3);"
    },
    "params": {
      "type": "batch"
    },
    "tags": {
      "name": "namedblock1--op2",
      "block": "namedblock1"
    }
  }
]
```

### lists of blocks as a list of property maps

When blocks are defined as a list of entries, each entry is a map.

*yaml:*

```yaml
blocks:
  - ops:
      op1: select * from bar.table;
      op2:
        type: batch
        stmt: insert into bar.table (a,b,c) values (1,2,3);
  - name: this-is-block-2
    ops:
      op3: select * from foo.table;

```

*json:*

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
    },
    {
      "name": "this-is-block-2",
      "ops": {
        "op3": "select * from foo.table;"
      }
    }
  ]
}
```

*ops:*

```json5
[
  {
    "name": "block1--op1",
    "op": {
      "stmt": "select * from bar.table;"
    },
    "tags": {
      "name": "block1--op1",
      "block": "block1"
    }
  },
  {
    "name": "block1--op2",
    "op": {
      "stmt": "insert into bar.table (a,b,c) values (1,2,3);"
    },
    "params": {
      "type": "batch"
    },
    "tags": {
      "name": "block1--op2",
      "block": "block1"
    }
  },
  {
    "name": "this-is-block-2--op3",
    "op": {
      "stmt": "select * from foo.table;"
    },
    "tags": {
      "name": "this-is-block-2--op3",
      "block": "this-is-block-2"
    }
  }
]
```

---

## Names

All documents, blocks, and ops within a workload can have an assigned name. When map and list forms
are both supported for entries, the map form provides the name. When list forms are used, an
additional field named `name` can be used.

*yaml:*

```yaml
blocks:
  - name: myblock
    op: "test op"

```

*json:*

```json5
{
  "blocks": [
    {
      "name": "myblock",
      "op": "test op"
    }
  ]
}
```

*ops:*

```json5
[
  {
    "name": "myblock--stmt1",
    "op": {
      "stmt": "test op"
    },
    "tags": {
      "name": "myblock--stmt1",
      "block": "myblock"
    }
  }
]
```

--

# Putting things together

This document is focused on the basic properties that can be added to a templated workload. To see
how they are combined together, see [templated_operations.md](templated_operations.md).
