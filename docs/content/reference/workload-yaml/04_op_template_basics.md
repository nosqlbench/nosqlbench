+++
title = "Op Templates"
description = "Auto-generated reference documentation for Op Templates"
weight = 144740440
template = "page.html"
date = 2025-11-13

[extra]
quadrant = "reference"
topic = "workload-yaml"
tags = ["auto-generated", "yaml", "specification", "workload"]
generator = "BundledMarkdownExporter"
source = "https://github.com/nosqlbench/nosqlbench/blob/main/nb-apis/adapters-api/src/main/java/workload_definition/04_op_template_basics.md"
+++

# Op Templates

Op templates are the recipes provided by users for an operation. These hold examples of payload
data, metadata that configures the driver, timeout settings and so on.

The field name used in workload templates to represent operations can often be symbolic to users.
For this reason, several names are allowed: ops, op, operations, statements, statement. It doesn't
matter whether the value is provided as a map, list, or scalar. These all allow for the same
level of templating. Map forms are preferred, since they include naming in a more streamlined
structure. When you use list form, you have to provide the name as a separate field.

A name is automatically provided by the API when there is one missing.

### a single un-named op template

*yaml:*

```yaml
op: select * from bar.table;
```

*json:*

```json

{
  "op": "select * from bar.table;"
}
```

*ops:*

```json

[
  {
    "name": "stmt1",
    "op": {
      "stmt": "select * from bar.table;"
    },
    "tags": {
      "name": "stmt1",
      "block": "block0",
      "op": "stmt1"
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

```json

{
  "ops": [
    "select * from bar.table;"
  ]
}
```

*ops:*

```json

[
  {
    "name": "stmt1",
    "op": {
      "stmt": "select * from bar.table;"
    },
    "tags": {
      "name": "stmt1",
      "block": "block0",
      "op": "stmt1"
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

```json

{
  "ops": [
    {
      "op1": "select * from bar.table;"
    }
  ]
}
```

*ops:*

```json

[
  {
    "name": "op1",
    "op": {
      "stmt": "select * from bar.table;"
    },
    "tags": {
      "name": "op1",
      "block": "block0",
      "op": "op1"
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

```json

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

```json

[
  {
    "name": "op1",
    "op": {
      "stmt": "select * from bar.table;"
    },
    "tags": {
      "name": "op1",
      "block": "block0",
      "op": "op1"
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

```json

{
  "ops": {
    "op1": "select * from bar.table;"
  }
}
```

*ops:*

```json

[
  {
    "name": "op1",
    "op": {
      "stmt": "select * from bar.table;"
    },
    "tags": {
      "name": "op1",
      "block": "block0",
      "op": "op1"
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

```json

{
  "ops": {
    "op1": {
      "stmt": "select * from bar.table;"
    }
  }
}
```

*ops:*

```json

[
  {
    "name": "op1",
    "op": {
      "stmt": "select * from bar.table;"
    },
    "tags": {
      "name": "op1",
      "block": "block0",
      "op": "op1"
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
      block: schema
    params:
      prepared: false
    description: This is just an example operation
```

*json:*

```json

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
        "block":"schema"
      }
    }
  }
}
```

*ops:*

```json

[
  {
    "bindings": {
      "binding1": "NumberNameToString();"
    },
    "description": "This is just an example operation",
    "name": "special-op-name",
    "op": {
      "stmt": "select * from ks1.tb1;"
    },
    "params": {
      "prepared": false
    },
    "tags": {
      "block": "block0",
      "name": "special-op-name",
      "op": "special-op-name"
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
  block_named_fred:
    bindings:
      binding1: NumberNameToString();
    tags:
      block: schema
    params:
      prepared: false
    description: This is just an example operation
    ops:
      op1:
        name: special-op-name
        op: select * from ks1.tb1;

```

*json:*

```json

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
    "block_named_fred": {
      "bindings": {
        "binding1": "NumberNameToString();"
      },
      "description": "This is just an example operation",
      "params": {
        "prepared": false
      },
      "tags": {
        "block": "schema"
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

```json

[
  {
    "bindings": {
      "binding1": "NumberNameToString();"
    },
    "name": "special-op-name",
    "op": {
      "stmt": "select * from ks1.tb1;"
    },
    "params": {
      "prepared": false
    },
    "tags": {
      "block": "block_named_fred",
      "docleveltag": "is-tagging-everything",
      "name": "special-op-name",
      "op": "special-op-name"
    }
  }
]
```


