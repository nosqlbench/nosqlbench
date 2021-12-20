# Op Templates Variations

These examples are here to illustrate and test specific variations of op templates.

## keyed name statement form

*yaml:*

```yaml
ops:
  op1:
    name: special-op-name
    op: select * from ks1.tb1;
    params:
      prepared: false
```

*json:*

```json5
{
  "ops": {
    "op1": {
      "name": "special-op-name",
      "op": "select * from ks1.tb1;",
      "params": {
        "prepared": false
      }
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
    "params": {
      "prepared": false
    },
    "tags": {
      "block": "block0",
      "name": "block0--special-op-name"
    }
  }
]
```

## keyed name statement-map form with name field

*yaml:*

```yaml
ops:
  op1:
    name: special-op-name
    op:
      field1: select * from ks1.tb1;
      field2: field 2 value
```

*json:*

```json5
{
  "ops": {
    "op1": {
      "name": "special-op-name",
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
    "name": "block0--special-op-name",
    "op": {
      "field1": "select * from ks1.tb1;",
      "field2": "field 2 value"
    },
    "tags": {
      "block": "block0",
      "name": "block0--special-op-name"
    }
  }
]
```

## keyed name statement-map form WITHOUT name field WITH op key

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

## keyed name statement-map form WITHOUT name field WITHOUT op key

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
      "stmt": "select * from ks1.tb1;"
    },
    "params": {
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
