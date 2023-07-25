# Template Variables

Template variables allow for workload descriptions to be parameterized outside the structure of the
templating language.

Template variables are resolved in the workload after the on-disk format is loaded and before yaml parsing.

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

## angle bracket value with defaults

This form is deprecated! It conflicts with the YAML syntax for anchors and aliases. It will be
removed in the next major version.

*yaml:*
```yaml
name: <<myname,thedefault>>
desc: <<mydesc:mydescription>>
```

*json:*
```json

{
    "name": "thedefault",
    "desc": "mydescription"
}
```

*ops:*
```json

[]
```
