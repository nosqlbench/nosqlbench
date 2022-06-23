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

```json5
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

```json5
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

```json5
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

```json5
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

```json5
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

```json5
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

```json5
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

```json5
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

```json5
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

```json5
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

```json5
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

```json5
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

