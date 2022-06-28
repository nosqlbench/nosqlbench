# Op Template Payloads

Payloads in NoSQLBench op templates can be of any type that you can create from bindings, string
templates, data structure, or any combination thereof. Yet, the recipe format for these bindings is
simple and direct. If you know what you want the op to do, then one of the supported forms here
should get you there.

The payload of an operation is handled as any content which is assigned to the `op` field in the
uniform op structure.

## assigned ops as string

When you are using the command, it is possible to specify `op=...` instead of providing a workload
path. When you do that, a workload description is automatically derived for you on-the-fly.

`nb run driver=stdout op='cycle number {{NumberNameToString}'`

The equivalent structure has the same effect as if you had created a workload description like this:

*yaml:*

```yaml
ops: "cycle number '{{NumberNameToString}}'"
```

*json:*

```json5
{
  "ops": "cycle number '{{NumberNameToString}}'"
}
```

*ops:*

```json5
[
  {
    "tags": {
      "name": "block0--stmt1",
      "block": "block0"
    },
    "op": {
      "stmt": "cycle number '{{NumberNameToString}}'"
    },
    "name": "block0--stmt1"
  }
]
```

## assigned ops as a list of strings

If the value type of the top-level ops field is in list form, then each value is processed
individually.

*yaml:*

```yaml
ops:
  - "even cycle '{{NumberNameToString}}'"
  - "odd  cycle '{{NumberNameToString}}'"
```

*json:*

```json5
{
  "ops": [
    "even cycle '{{NumberNameToString}}'",
    "odd  cycle '{{NumberNameToString}}'"
  ]
}
```

*ops:*

```json5
[
  {
    "tags": {
      "name": "block0--stmt1",
      "block": "block0"
    },
    "op": {
      "stmt": "even cycle '{{NumberNameToString}}'"
    },
    "name": "block0--stmt1"
  },
  {
    "tags": {
      "name": "block0--stmt2",
      "block": "block0"
    },
    "op": {
      "stmt": "odd  cycle '{{NumberNameToString}}'"
    },
    "name": "block0--stmt2"
  }
]
```

## assigned ops as a map of strings

A more preferable way to add ops to a structure is in map form. This is also covered elsewhere, but
it is important for examples further below so we'll refresh it here:

*yaml:*

```yaml
ops:
  myop1: "even cycle '{{NumberNameToString}}'"
  myop2: "odd  cycle '{{NumberNameToString}}'"
```

*json:*

```json5
{
  "ops": {
    "myop1": "even cycle '{{NumberNameToString}}'",
    "myop2": "odd  cycle '{{NumberNameToString}}'"
  }
}
```

*ops:*

```json5
[
  {
    "tags": {
      "name": "block0--myop1",
      "block": "block0"
    },
    "op": {
      "stmt": "even cycle '{{NumberNameToString}}'"
    },
    "name": "block0--myop1"
  },
  {
    "tags": {
      "name": "block0--myop2",
      "block": "block0"
    },
    "op": {
      "stmt": "odd  cycle '{{NumberNameToString}}'"
    },
    "name": "block0--myop2"
  }
]
```

## assigned op payload as a simple map of op fields

When your operation takes on a non-statement form, you simply provide a map structure at the
top-level of the op:

*yaml:*

```yaml
ops:
  op1:
    opfield1: opvalue1
    opfield2: opvalue2


```

*json:*

```json5
{
  "ops": {
    "op1": {
      "opfield1": "opvalue1",
      "opfield2": "opvalue2"
    }
  }
}
```

*ops:*

```json5
[
  {
    "tags": {
      "name": "block0--op1",
      "block": "block0"
    },
    "op": {
      "opfield1": "opvalue1",
      "opfield2": "opvalue2"
    },
    "name": "block0--op1"
  }
]
```

## assigned op payload as an array of values

When your operation takes on a non-statement form, you simply provide a map structure at the
top-level of the op. Notice that the structurally normalized form shows the field values moved
underneath the canonical 'op' field by default. This is because the structurally normalized
op template form *always* has a map in the op field. The structural short-hand for creating an
op template that is list based simply moves any list entries at the op template level down in
the named 'op' field for you.

*yaml:*

```yaml
ops:
  op1:
    - opvalue1
    - opvalue2
```

*json:*

```json5
{
  "ops": {
    "op1": [
      "opvalue1",
      "opvalue2"
    ]
  }
}
```

*ops:*

```json5
[
  {
    "tags": {
      "name": "block0--op1",
      "block": "block0"
    },
    "op": {
      "stmt": [
        "opvalue1",
        "opvalue2"
      ]
    },
    "name": "block0--op1"
  }
]
```

## op payload with structured value types

From the examples above, it's clear that you can use string and maps in your op fields. Here, we
show how you can use arbitrary levels of structured types based on commodity collection types like
List and Map for Java and JSON objects and arrays.

*yaml:*

```yaml
ops:
  op1:
    index_map:
      username: user_index
      geocode: georef_v23
    rollups:
      - by_username/@4h
      - by_session_len/@1h
```

*json:*
```json5
{
  "ops": {
    "op1": {
      "index_map": {
        "username": "user_index",
        "geocode": "georef_v23"
      },
      "rollups": [
        "by_username/@4h",
        "by_session_len/@1h"
      ]
    }
  }
}
```

*ops:*

```json5
[
  {
    "tags": {
      "name": "block0--op1",
      "block": "block0"
    },
    "op": {
      "index_map": {
        "username": "user_index",
        "geocode": "georef_v23"
      },
      "rollups": [
        "by_username/@4h",
        "by_session_len/@1h"
      ]
    },
    "name": "block0--op1"
  }
]

```

## Binding Points at any position in op field data

You can use either named binding points references like `{userid}` or binding definitions such
as `{{Template('user-{}',ToString())}}`. The only exception to this rule is that you may not
(yet) use dynamic values for keys within your structure.

*yaml:*

```yaml
bindings:
  user_index: Mod(1000L); ToString();

ops:
  op1:
    index_map:
      username: "{user_index}"
      geocode: "{{Template('georef_v{}',HashRange(0,23))}}"
    rollups:
      - by_username/@4h
      - by_session_len/@1h
```

*json:*
```json5
{
  "bindings": {
    "user_index": "Mod(1000L); ToString();"
  },
  "ops": {
    "op1": {
      "index_map": {
        "username": "{user_index}",
        "geocode": "{{Template('georef_v{}',HashRange(0,23))}}"
      },
      "rollups": [
        "by_username/@4h",
        "by_session_len/@1h"
      ]
    }
  }
}
```

*ops:*

```json5
[
  {
    "bindings": {
      "user_index": "Mod(1000L); ToString();"
    },
    "tags": {
      "name": "block0--op1",
      "block": "block0"
    },
    "op": {
      "index_map": {
        "username": "{user_index}",
        "geocode": "{{Template('georef_v{}',HashRange(0,23))}}"
      },
      "rollups": [
        "by_username/@4h",
        "by_session_len/@1h"
      ]
    },
    "name": "block0--op1"
  }
]
```



