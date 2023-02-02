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

It's easier on syntax checkers if you use this form.

## call form with defaults

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
