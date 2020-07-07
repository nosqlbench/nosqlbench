---
title: 01 Statement Templates
weight: 01
---

# Statement Templates

A valid config file for an activity consists of statement templates, parameters for them, bindings to generate the data
to use with them, and tags for organizing them.

In essence, the config format is *all about configuring statements*. Every other element in the config format is in some
way modifying or otherwise helping create statements to be used in an activity.

Statement templates are the single most important part of a YAML config.

```yaml
# a single statement
statements:
 - a single statement body
```

This is a valid activity YAML file in and of itself. It has a single statement template.

It is up to the individual activity types like _cql_, or _stdout_ to interpret the statement template in some way. The
example above is valid as a statement in the stdout activity, but it does not produce a valid CQL statement with the CQL
activity type. The contents of the statement template are free form text. If the statement template is valid CQL, then
the CQL activity type can use it without throwing an error. Each activity type determines what a statement means, and
how it will be used.

You can provide multiple statements, and you can use the YAML pipe to put them on multiple lines, indented a little
further in:

```yaml
statements:
 - |
  This is a statement, and the file format doesn't
  know how statements will be used!
 - |
  submit job {alpha} on queue {beta} with options {gamma};
```

Statements can be named:

```yaml
statements:
 - s1: |
  This is a statement, and the file format doesn't
  know how statements will be used!
 - s2: |
  submit job {alpha} on queue {beta} with options {gamma};
```

Actually, every statement in a YAML has a name. If you don't provide one, then a name is auto-generated for the
statement based on its position in the YAML file.

