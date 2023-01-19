---
title: Op Template Variations
---

# Op Templates Variations

These examples illustrate a variety of valid op template structures.

## Op Naming

### map of op templates with explicit name

If you use a map of op templates, they can still override the name of the op simply by adding
the `name` key.

*yaml:*

```yaml
ops:
  op1:
    name: special-op-name
    op: select * from ks1.tb1;
```

*json:*

```json

{
  "ops": {
    "op1": {
      "name": "special-op-name",
      "op": "select * from ks1.tb1;"
    }
  }
}
```

*ops:*

```json

[
  {
    "name": "block0--special-op-name",
    "op": {
      "stmt": "select * from ks1.tb1;"
    },
    "tags": {
      "block": "block0",
      "name": "block0--special-op-name"
    }
  }
]
```

### map of op templates without explicit name

This yaml document contains a single named op template `op1` which contains a scoped op `op` with
two op fields `field1` and `field2`.

The op template takes its name `op1` from the map key under the ops property.

*yaml:*

```yaml
ops:
  op1:
    op:
      field1: select * from ks1.tb1;
      field2: field 2 value
```

*json:*

```json

{
  "ops": {
    "op1": {
      "op": {
        "field1": "select * from ks1.tb1;",
        "field2": "field 2 value"
      }
    }
  }
}
```

*ops:*

```json

[
  {
    "name": "block0--op1",
    "op": {
      "field1": "select * from ks1.tb1;",
      "field2": "field 2 value"
    },
    "tags": {
      "block": "block0",
      "name": "block0--op1"
    }
  }
]
```

## Op Fields

### Anonymous fields go to op by default

*yaml:*

```yaml
ops:
  op1:
    field1: select * from ks1.tb1;
    field2: field 2 value
```

*json:*

```json

{
  "ops": {
    "op1": {
      "field1": "select * from ks1.tb1;",
      "field2": "field 2 value"
    }
  }
}
```

*ops:*

```json

[
  {
    "name": "block0--op1",
    "op": {
      "field1": "select * from ks1.tb1;",
      "field2": "field 2 value"
    },
    "tags": {
      "block": "block0",
      "name": "block0--op1"
    }
  }
]
```

### Anonymous fields may include scoped params

*yaml:*

```yaml
ops:
  op1:
    field1: select * from ks1.tb1;
    field2: field 2 value
    params:
      paramname1: paramvalue1
```

*json:*

```json

{
  "ops": {
    "op1": {
      "field1": "select * from ks1.tb1;",
      "field2": "field 2 value",
      "params": {
        "paramname1": "paramvalue1"
      }
    }
  }
}
```

*ops:*

```json

[
  {
    "name": "block0--op1",
    "op": {
      "field1": "select * from ks1.tb1;",
      "field2": "field 2 value"
    },
    params: {
      "paramname1": "paramvalue1"
    },
    "tags": {
      "block": "block0",
      "name": "block0--op1"
    }
  }
]
```

### Scoped op fields allow dangling param values

*yaml:*

```yaml
ops:
  op1:
    op:
      field1: select * from ks1.tb1;
      field2: field 2 value
    paramname1: paramval1
```

*json:*

```json

{
  "ops": {
    "op1": {
      "op": {
        "field1": "select * from ks1.tb1;",
        "field2": "field 2 value"
      },
      "paramname1": "paramval1"
    }
  }
}
```

*ops:*

```json

[
  {
    "name": "block0--op1",
    "op": {
      "field1": "select * from ks1.tb1;",
      "field2": "field 2 value"
    },
    "params": {
      "paramname1": "paramval1"
    },
    "tags": {
      "block": "block0",
      "name": "block0--op1"
    }
  }
]
```

### Scoped op and param fields disallow dangling fields

*yaml:*

```yaml
ops:
  op1:
    op:
      field1: select * from ks1.tb1;
      field2: field 2 value
    params:
      paramname1: paramval1
#   dangling1: value
#   ^ NOT ALLOWED HERE
```

*json:*

```json

{
  "ops": {
    "op1": {
      "op": {
        "field1": "select * from ks1.tb1;",
        "field2": "field 2 value"
      },
      "params": {
        "paramname1": "paramval1"
      }
    }
  }
}
```

*ops:*

```json

[
  {
    "name": "block0--op1",
    "op": {
      "field1": "select * from ks1.tb1;",
      "field2": "field 2 value"
    },
    "params": {
      "paramname1": "paramval1"
    },
    "tags": {
      "block": "block0",
      "name": "block0--op1"
    }
  }
]
```

## params at doc level

*yaml:*

```yaml
params:
  pname: pvalue
ops: "my test op"
```

*json:*

```json

{
  "params": {
    "pname": "pvalue"
  },
  "ops": "my test op"
}
```

*ops:*

```json

[
  {
    "params": {
      "pname": "pvalue"
    },
    "tags": {
      "name": "block0--stmt1",
      "block": "block0"
    },
    "op": {
      "stmt": "my test op"
    },
    "name": "block0--stmt1"
  }
]
```

## params at block level

*yaml:*

```yaml
blocks:
  block1:
    params:
      pname: pvalue
    ops: "my test op"
```

*json:*

```json

{
  "blocks": {
    "block1": {
      "params": {
        "pname": "pvalue"
      },
      "ops": "my test op"
    }
  }
}
```

*ops:*

```json

[
  {
    "params": {
      "pname": "pvalue"
    },
    "tags": {
      "name": "block1--stmt1",
      "block": "block1"
    },
    "op": {
      "stmt": "my test op"
    },
    "name": "block1--stmt1"
  }
]
```

## params at op level

*yaml:*

```yaml
blocks:
  block1:
    ops:
      op1:
        op: "my test op"
        params:
          pname: pvalue

```

*json:*

```json

{
  "blocks": {
    "block1": {
      "ops": {
        "op1": {
          "op": "my test op",
          "params": {
            "pname": "pvalue"
          }
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
    "params": {
      "pname": "pvalue"
    },
    "tags": {
      "name": "block1--op1",
      "block": "block1"
    },
    "op": {
      "stmt": "my test op"
    },
    "name": "block1--op1"
  }
]
```

## params field at op field level is not treated as special

When you put your params within the op fields by name, alongside the other op fields, it is not
treated specially. This is not disallowed, as there may be scenarios where this is otherwise a valid
value. Further, params within the op field would not provide any benefit over simply having those
named values in the op field directly, as this is consulted first for dynamic and static values.

*yaml:*

```yaml
blocks:
  block1:
    ops:
      op1:
        op:
          stmt: "my test op"
          params:
            pname: pvalue

```

*json:*

```json

{
  "blocks": {
    "block1": {
      "ops": {
        "op1": {
          "op": {
            "stmt": "my test op",
            "params": {
              "pname": "pvalue"
            }
          }
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
    "tags": {
      "name": "block1--op1",
      "block": "block1"
    },
    "op": {
      "stmt": "my test op",
      "params": {
        "pname": "pvalue"
      }
    },
    "name": "block1--op1"
  }
]
```

## params field at op name level is not treated as special

When you are using map-based op template names, and one of them has a name of 'param', it is treated
just as any other op template name. The fields will not be recognized as param names and values,
but as op template fields.

*yaml:*

```yaml
blocks:
  block1:
    ops:
      op1:
        op:
          stmt: "my test op"
      params:
        pname: pvalue
```

*json:*

```json

{
  "blocks": {
    "block1": {
      "ops": {
        "op1": {
          "op": {
            "stmt": "my test op"
          }
        },
        "params": {
          "pname": "pvalue"
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
    "tags": {
      "name": "block1--op1",
      "block": "block1"
    },
    "op": {
      "stmt": "my test op"
    },
    "name": "block1--op1"
  },
  {
    "tags": {
      "name": "block1--params",
      "block": "block1"
    },
    "op": {
      "pname": "pvalue"
    },
    "name": "block1--params"
  }
]
```






