+++
title = "Op Templates"
description = "Learn how to create operation templates in workload files"
weight = 20
template = "docs-page.html"

[extra]
quadrant = "tutorials"
topic = "workload-basics"
category = "fundamentals"
tags = ["workloads", "yaml", "operations", "templates"]
+++

Op templates are data blueprints that represent a particular kind of operation. They are
templates because they are used to create possibly many operations to be executed. For example,
you may have only one op template that you use to drive a billion operations to a system under
test, or you may have a myriad of different access patterns. In either case, all the
operations that you want to execute must be defined in template form beforehand.

As templates, you indicate where the variable parts are filled in when needed. How this is done
will be explained in the [data bindings](03-data-bindings.md) section.

# Simple statements

In essence, the config format is *all about configuring operations*. Every other element in the
config format is in some way modifying or otherwise helping create operations to be used in an
activity.

Op templates are the single most important part of a YAML config.

```yaml
# a single operation
ops:
  - a single statement body
```

This is a valid activity YAML file in and of itself. It has a single op template.

It is up to the individual activity types like _cql_, or _stdout_ to interpret the op template in
some way. The example above is valid as an operation in the stdout activity, but it does not produce
a valid CQL statement when used with the CQL activity type. The contents of the op can be provided
as free-form text. If the op template is valid CQL, then the CQL activity type can use it without
throwing an error. Each activity type determines what a statement means, and how it will be used.

## Multiple Op Templates

You can specify multiple op templates:

```yaml
ops:
  - This is a statement, and the file format doesn't know how statements will be used!
  - submit job {alpha} on queue {beta} with options {gamma};
```

## YAML formatting prefixes

You can use the YAML pipe to put them on multiple lines, indented a little further in:

```yaml
ops:
  - |
    This is a statement, and the file format doesn't
    know how statements will be used!
  - |
    submit job {alpha} on queue {beta} with options {gamma};
```

There are a few other [useful YAML line prefixes](https://yaml.org/spec/1.2.2/#63-line-prefixes)
which behave slightly differently.

## Op Naming

Op templates can be named:

```yaml
ops:
  - s1: |
  This is a statement, and the file format doesn't
  know how statements will be used!
  - s2: |
  submit job {alpha} on queue {beta} with options {gamma};
```

Actually, every op template in a YAML file has a name. If you don't provide one, then a name is
auto-generated for the op template based on its position in the YAML file.
