+++
title = "Data Bindings"
description = "Learn how to use data bindings for generating test data"
weight = 40
template = "page.html"

[extra]
quadrant = "tutorials"
topic = "workload-basics"
category = "data-generation"
tags = ["workloads", "bindings", "virtdata", "data-generation"]
+++

## Virtual Data Set

Procedural data generation is built-in to the NoSQLBench runtime by way of the
_Virtual Data Set_ library. This allows us to create named data generation recipes. These named
recipes for generated data are called bindings. Procedural generation for test data has
many benefits over shipping bulk test data around,
including speed and deterministic behavior. With the _Virtual Data Set_ approach, most of the hard
work is already done for us. We just have to pull in the recipes we want.

## Bindings Syntax

You can add a bindings section like this:

```yaml
bindings:
  alpha: Identity()
  beta: NumberNameToString()
  gamma: Combinations('0-9A-F;0-9;A-Z;_;p;r;o;')
  delta: WeightedStrings('one:1;six:6;three:3;')
```

This is a YAML map which provides names and function specifiers. The first binding is named_alpha_,
and calls an _Identity_ function that takes an input value and returns the same value. Together, the
name and value constitute a binding named alpha. All four bindings together are called a bindings
set.

The above bindings block is also a valid activity YAML, at least for the _stdout_ activity type.
The _stdout_ activity can construct a statement template from the provided bindings if needed, so
this is valid:

```shell
[test]$ cat > stdout-test.yaml
    bindings:
     alpha: Identity()
     beta: NumberNameToString()
     gamma: Combinations('0-9A-F;0-9;A-Z;_;p;r;o;')
     delta: WeightedStrings('one:1;six:6;three:3;')
# EOF (control-D in your terminal)

[test]$ ./nb5 run driver=stdout workload=stdout-test cycles=10
0,zero,00A_pro,six
1,one,00B_pro,six
2,two,00C_pro,three
3,three,00D_pro,three
4,four,00E_pro,six
5,five,00F_pro,six
6,six,00G_pro,six
7,seven,00H_pro,six
8,eight,00I_pro,six
9,nine,00J_pro,six
```

Above, you can see that the stdout activity type is ideal for experimenting with data generation
recipes. It uses the default `format=csv` parameter above, but it also supports formats like json,
inlinejson, readout, and assignments.

This is all you need to provide a formulaic recipe for converting an ordinal value to a set of field
values. Each time NoSQLBench needs to create a set of values as parameters to a statement, the
binding functions are called with an input, known as the cycle. The functions produce a set of named
values that, when combined with a statement template, can yield an individual statement for a
database operation. In this way, each cycle represents a specific operation. Since the functions
above are pure functions, the cycle number of an operation will always produce the same operation,
thus making all NoSQLBench workloads that use pure functions deterministic.

In the example above, you can see the cycle numbers down the left.

## Binding Anchors

If you combine the op template section and the bindings sections above into one activity yaml, you
get a slightly different result, as the bindings apply to the operations that are provided, rather
than creating a default op template for all provided bindings. See the example below:

```yaml
# stdout-test.yaml
statements:
 - |
  This is a statement, and the file format doesn't
  know how statements will be used!
 - |
  submit job {alpha} on queue {beta} with options {gamma};
bindings:
 alpha: Identity()
 beta: NumberNameToString()
 gamma: Combinations('0-9A-F;0-9;A-Z;_;p;r;o;')
 delta: WeightedStrings('one:1;six:6;three:3;')
```

```shell
[test]$ ./nb5 run driver=stdout workload=stdout-test cycles=10
This is a statement, and the file format doesn't
know how statements will be used!
submit job 1 on queue one with options 00B_pro;
This is a statement, and the file format doesn't
know how statements will be used!
submit job 3 on queue three with options 00D_pro;
This is a statement, and the file format doesn't
know how statements will be used!
submit job 5 on queue five with options 00F_pro;
This is a statement, and the file format doesn't
know how statements will be used!
submit job 7 on queue seven with options 00H_pro;
This is a statement, and the file format doesn't
know how statements will be used!
submit job 9 on queue nine with options 00J_pro;
```

There are a few things to notice here. First, the statements that are executed are automatically
alternated between. If you had 10 different operations listed, they would all get their turn with 10
cycles. Since there were two, each was run 5 times.

Also, the op templates that had named anchors acted as a template, whereas the other one was
evaluated just as it was. In fact, they were both treated as templates, but one of them had no
anchors.

One more minor but important detail is that the fourth binding *delta* was not referenced directly
in the statements. Since the op templates did not pair up an anchor with this binding name, it was
not used. No values were generated for it.

Bindings are templates for data generation, only to be used when necessary. Bindings that are
defined nearby an op template are like a menu of data generation options. If the op template
references those bindings with `{named_anchors}`, then the recipes will be used to construct data
when that op template is selected for a specific cycle. The cycle number both selects the
operation (via the op sequence) and also provides the input value as the initial input to the
binding functions.

## Further Details

A deeper explanation of binding concepts can be found in the [Binding Concepts](../../reference/bindings/)
part of the Reference Section, where you will also find documentation about how to use the various
binding functions that are available.
