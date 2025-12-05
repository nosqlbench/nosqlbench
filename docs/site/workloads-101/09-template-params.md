---
title: 09 Template Params
description: Template Params Defined
tags:
- site
- docs
audience: user
diataxis: howto
component: site
topic: docops
status: live
owner: '@nosqlbench/docs'
generated: false
weight: 109
sort_by: weight
---

## Template Params Defined

All NoSQLBench YAML formats support a parameter macro format that applies before YAML processing
starts. It is a basic macro facility that allows named anchors to be placed in the document as a
whole:

## Template Param Formats

Template params can be provided with a name and a default value in one of these forms:

```
<<varname:defaultval>>
```
or
```
TEMPLATE(varname,defaultval)
```

In this example, the name of the parameter is `varname`. It is given a default value of `defaultval`
. If an activity parameter named *varname* is provided, as in `varname=barbaz`, then this whole
expression will be replaced with
`barbaz`. If none is provided then the default value will be used instead. For example:

## Shared Namespace

You must ensure that your template params do not overlap with the names of other parameters if 
you want to avoid an error. NoSQLBench makes it possible for drivers to 
detect when unrecognized parameters are provided to a driver or op template. As such, when 
template parameters are accessed from configuration sources, they are also consumed. This is to 
ensure unambiguous usage of every parameter. 

## Template Param Examples

```yaml
[ test ]$ cat > stdout-test.yaml
statements:
  - "<<linetoprint:MISSING>>\n"
  # EOF (control-D in your terminal)

  [ test ]$ ./nb5 run driver=stdout workload=stdout-test cycles=1
  MISSING

  [ test ]$ ./nb5 run driver=stdout workload=stdout-test cycles=1 linetoprint="THIS IS IT"
  THIS IS IT
```

If an empty value is desired by default, then simply use an empty string in your template,
like `<<varname:>>` or
`TEMPLATE(varname,)`.


