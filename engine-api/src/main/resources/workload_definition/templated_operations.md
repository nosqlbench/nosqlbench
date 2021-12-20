# Op Templates

The rules around op templates deserve a separate section, given that there are many techniques that
a user can choose from.

The valid elements of the raw workload form are explained below, using YAML and JSON5 as a schematic
language. This guide is not meant to be very explanatory for new users, but it can serve as a handy
reference about how workloads can be structured.

Any bundled workload loader should test all of these fenced code blocks and confirm that the data
structures are logically equivalent, using any json5 blocks as a trigger to compare against the
prior block.
**This document is a testable specification.**

While some of the examples below appear to be demonstrating basic cross-format encoding, there is
more going on. This document captures a set of basic sanity rules for all raw workload data, as well
as visual examples and narratives to assist users and maintainers. In short, if you don't see what
you want to do here, it is probably not valid in the format, and if you know that to be false, then
this document needs to be updated with more details!

The field used in workload templates to represent an operation can often be symbolic to users. For
this reason, several names are allowed: ops, op, operations, statements, statement. It doesn't
matter whether the value is provided as a map, list, or scalar. These all do the same thing,
although an error is thrown if you specify more than one. The interpretation is always the same: An
ordered collection of op templates. In map forms, the key is the op name. In forms which contain no
provided name (as a key or as a property of an element map), a name is automatically provided by the
API.

### a single un-named op template

*yaml:*

```yaml
op: select * from bar.table;
```

*json:*

```json5
{
  "op": "select * from bar.table;"
}
```

*ops:*

```json5
[
  {
    "name": "block0--stmt1",
    "op": {
      "stmt": "select * from bar.table;"
    },
    "tags": {
      "name": "block0--stmt1",
      "block": "block0"
    }
  }
]
```

### un-named op templates as a list of strings

*yaml:*

```yaml
ops:
  - select * from bar.table;
```

*json:*

```json5
{
  "ops": [
    "select * from bar.table;"
  ]
}
```

*ops:*

```json5
[
  {
    "name": "block0--stmt1",
    "op": {
      "stmt": "select * from bar.table;"
    },
    "tags": {
      "name": "block0--stmt1",
      "block": "block0"
    }
  }
]
```

### named op templates as a list of maps with name-stmt as first entry

This form will take the first key and value of the map as the name and statement for the op
template.

*yaml:*

```yaml
ops:
  - op1: select * from bar.table;
```

*json:*

```json5
{
  "ops": [
    {
      "op1": "select * from bar.table;"
    }
  ]
}
```

*ops:*

```json5
[
  {
    "name": "block0--op1",
    "op": {
      "stmt": "select * from bar.table;"
    },
    "tags": {
      "name": "block0--op1",
      "block": "block0"
    }
  }
]
```

### op templates as a list of maps with name field

*yaml:*

```yaml
ops:
  - name: op1
    op: select * from bar.table;
```

*json:*

```json5
{
  "ops": [
    {
      "name": "op1",
      "op": "select * from bar.table;"
    }
  ]
}
```

*ops:*

```json5
[
  {
    "name": "block0--op1",
    "op": {
      "stmt": "select * from bar.table;"
    },
    "tags": {
      "name": "block0--op1",
      "block": "block0"
    }
  }
]
```

### named op templates as a map of strings

*yaml:*

```yaml
ops:
  op1: select * from bar.table;
```

*json:*

```json5
{
  "ops": {
    "op1": "select * from bar.table;"
  }
}
```

*ops:*

```json5
[
  {
    "name": "block0--op1",
    "op": {
      "stmt": "select * from bar.table;"
    },
    "tags": {
      "name": "block0--op1",
      "block": "block0"
    }
  }
]
```

### named op templates as a map of maps

*yaml:*

```yaml
ops:
  op1:
    stmt: select * from bar.table;
```

*json:*

```json5
{
  "ops": {
    "op1": {
      "stmt": "select * from bar.table;"
    }
  }
}
```

*ops:*

```json5
[
  {
    "name": "block0--op1",
    "op": {
      "stmt": "select * from bar.table;"
    },
    "tags": {
      "name": "block0--op1",
      "block": "block0"
    }
  }
]
```

# Op Template Properties

All the forms above merely show how you can structure op templates into common collection forms and
have them be interpreted in a flexible yet obvious way.

However, all the properties described in [templated_workloads.md](templated_workloads.md)
can be attached directly to op templates too. This section contains a few examples to illustrate
this at work.

## detailed op template example

*yaml:*

```yaml
ops:
  op1:
    name: special-op-name
    op: select * from ks1.tb1;
    bindings:
      binding1: NumberNameToString();
    tags:
      phase: schema
    params:
      prepared: false
    description: This is just an example operation
```

*json:*

```json5
{
  "ops": {
    "op1": {
      "bindings": {
        "binding1": "NumberNameToString();"
      },
      "description": "This is just an example operation",
      "name": "special-op-name",
      "op": "select * from ks1.tb1;",
      "params": {
        "prepared": false
      },
      "tags": {
        "phase": "schema"
      }
    }
  }
}
```

*ops:*

```json5
[
  {
    "bindings": {
      "binding1": "NumberNameToString();"
    },
    "description": "This is just an example operation",
    "name": "block0--special-op-name",
    "op": {
      "stmt": "select * from ks1.tb1;"
    },
    "params": {
      "prepated": false
    },
    "tags": {
      "phase": "schema",
      "name": "block0--special-op-name",
      "block": "block0"
    }
  }
]
```

# Property Layering

Properties that are provided at the top (doc) level become defaults for each nested layer (block or
ops). Each named binding, param, or tag is automatically assigned to any contained layers which do
not have one of the same name. When two layers contain the same named binding, param or tag, the
inner-most scope decides the value seen at the op level.

## block-level defaults and overrides

*yaml:*

```yaml
tags:
  docleveltag: is-tagging-everything # applies to all operations in this case

bindings:
  binding1: Identity(); # will be overridden at the block level

params:
  prepared: true # set prepared true by default for all contained op templates

blocks:
  block-named-fred:
    bindings:
      binding1: NumberNameToString();
    tags:
      phase: schema
    params:
      prepared: false
    description: This is just an example operation
    ops:
      op1:
        name: special-op-name
        op: select * from ks1.tb1;

```

*json:*

```json5
{
  "tags": {
    "docleveltag": "is-tagging-everything"
  },
  "bindings": {
    "binding1": "Identity();"
  },
  "params": {
    "prepared": true
  },
  "blocks": {
    "block-named-fred": {
      "bindings": {
        "binding1": "NumberNameToString();"
      },
      "description": "This is just an example operation",
      "params": {
        "prepared": false
      },
      "tags": {
        "phase": "schema"
      },
      "ops": {
        "op1": {
          "name": "special-op-name",
          "op": "select * from ks1.tb1;"
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
    "bindings": {
      "binding1": "NumberNameToString();"
    },
    "name": "block-named-fred--special-op-name",
    "op": {
      "stmt": "select * from ks1.tb1;"
    },
    "params": {
      "prepared": false
    },
    "tags": {
      "phase": "schema",
      "docleveltag": "is-tagging-everything",
      "name": "block-named-fred--special-op-name",
      "block": "block-named-fred"
    }
  }
]
```


