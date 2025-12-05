---
source: nb-apis/adapters-api/src/main/java/workload_definition/07_template_variables.md
title: "07 Template Variables"
description: "API doc for 07 template variables."
tags:
  - api
  - docs
audience: developer
diataxis: reference
component: core
topic: api
status: live
owner: "@nosqlbench/devrel"
generated: false
---
# Template Variables

Template variables allow for workload descriptions to be parameterized outside the structure of the
templating language.

Template variables are resolved in the workload after the on-disk format is loaded and before yaml parsing.

**Note**: As of version 5.x, the double angle bracket syntax (`<<...>>`) has been removed. Use the
`TEMPLATE(...)` function form instead. This change improves syntax highlighting and IDE integration
by avoiding conflicts with YAML anchors and aliases.

## call form with defaults

This is the preferred form. It's easier on syntax checkers.


*yaml:*
```yaml
name: TEMPLATE(myname,thedefault)
```

*json:*
```json

{
    "name": "thedefault"
}
```

*ops:*
```json

[]
```

## call form with no default, requires input

*yaml:*
```yaml
name: TEMPLATE(myname)
```

*json:*
```json

{
    "name": "UNSET:myname"
}
```

*ops:*
```json

[]
```

## call form with null default

*yaml:*
```yaml
name: TEMPLATE(myname,)
```

*json:*
```json

{
    "name": null
}
```

*ops:*
```json

[]
```

## call form with default value specified once

*yaml:*
```yaml
name: TEMPLATE(myname,default)
description: This is the description for name 'TEMPLATE(myname)'
```

*json:*
```json

{
    "name": default,
    "description": "This is the description for name 'default'"
}
```

*ops:*
```json

[]
```

## angle bracket value with defaults (REMOVED)

**Note**: This form has been removed as of version 5.x. The double angle bracket syntax (`<<...>>`)
conflicted with YAML syntax for anchors and aliases, and caused issues with syntax highlighting
and IDE integration. Please use the `TEMPLATE(...)` function form instead.

Previously supported syntax (no longer available):
```yaml
# REMOVED - use TEMPLATE(myname,thedefault) instead
name: <<myname,thedefault>>
desc: <<mydesc:mydescription>>
```
