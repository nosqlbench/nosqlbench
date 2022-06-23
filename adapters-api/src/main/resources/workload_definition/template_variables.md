# Template Variables

Template variables allow for workload descriptions to be parameterized outside the structure of the
templating language.

Template variables are resolved in the workload after the on-disk format is loaded and before yaml parsing.

## angle bracket value with defaults

*yaml:*
```yaml
name: <<myname,thedefault>>
desc: <<mydesc:mydescription>>
```

*json:*
```json5
{
    "name": "thedefault",
    "desc": "mydescription"
}
```

*ops:*
```json5
[]
```

It's easier on syntax checkers if you use this form.

## call form with defaults

*yaml:*
```yaml
name: TEMPLATE(myname,thedefault)
```

*json:*
```json5
{
    "name": "thedefault"
}
```

*ops:*
```json5
[]
```

## call form with no default, requires input

*yaml:*
```yaml
name: TEMPLATE(myname)
```

*json:*
```json5
{
    "name": "UNSET:myname"
}
```

*ops:*
```json5
[]
```

## call form with null default

*yaml:*
```yaml
name: TEMPLATE(myname,)
```

*json:*
```json5
{
    "name": null
}
```

*ops:*
```json5
[]
```
