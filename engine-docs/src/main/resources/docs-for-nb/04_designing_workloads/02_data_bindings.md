---
title: 02 Data Bindings
weight: 02
---

## Data Bindings

Procedural data generation is built-in to the nosqlbench runtime by way of the [Virtual DataSet](http://virtdata.io/) library. This allows us to create named data generation recipes. These named recipes for generated data are called bindings. Procedural generation for test data has [many benefits](http://docs.virtdata.io/why_virtdata/why_virtdata/) over shipping bulk test data around, including speed and deterministic behavior. With the VirtData approach, most of the hard work is already done for us. We just have to pull in the recipes we want.

You can add a bindings section like this:

```yaml
bindings:
 alpha: Identity()
 beta: NumberNameToString()
 gamma: Combinations('0-9A-F;0-9;A-Z;_;p;r;o;')
 delta: WeightedStrings('one:1;six:6;three:3;')
```

This is a YAML map which provides names and function specifiers. The specifier named _alpha_ provides a function that takes an input value and returns the same value. Together, the name and value constitute a binding named alpha. All of the four bindings together are called a bindings set.

The above bindings block is also a valid activity YAML, at least for the _stdout_ activity type. The _stdout_ activity can construct a statement template from the provided bindings if needed, so this is valid:

```text
[test]$ cat > stdout-test.yaml
    bindings:
     alpha: Identity()
     beta: NumberNameToString()
     gamma: Combinations('0-9A-F;0-9;A-Z;_;p;r;o;')
     delta: WeightedStrings('one:1;six:6;three:3;')
# EOF (control-D in your terminal)

[test]$ ./nb run driver=stdout workload=stdout-test cycles=10
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

Above, you can see that the stdout activity type is idea for experimenting with data generation recipes. It uses the default `format=csv` parameter above, but it also supports formats like json, inlinejson, readout, and assignments.

This is all you need to provide a formulaic recipe for converting an ordinal value to a set of field values. Each time nosqlbench needs to create a set of values as parameters to a statement, the functions are called with an input, known as the cycle. The functions produce a set of named values that, when combined with a statement template, can yield an individual statement for a database operation. In this way, each cycle represents a specific operation. Since the functions above are pure functions, the cycle number of an operation will always produce the same operation, thus making all nosqlbench workloads deterministic.

In the example above, you can see the cycle numbers down the left.

If you combine the statement section and the bindings sections above into one activity yaml, you get a slightly different result, as the bindings apply to the statements that are provided, rather than creating a default statement for the bindings. See the example below:

```text
[test]$ cat > stdout-test.yaml
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
# EOF (control-D in your terminal)

[test]$ ./nb run driver=stdout workload=stdout-test cycles=10
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

There are a few things to notice here. First, the statements that are executed are automatically alternated between. If you had 10 different statements listed, they would all get their turn with 10 cycles. Since there were two, each was run 5 times.

Also, the statement that had named anchors acted as a template, whereas the other one was evaluated just as it was. In fact, they were both treated as templates, but one of them had no anchors.

On more minor but important detail is that the fourth binding *delta* was not referenced directly in the statements. Since the statements did not pair up an anchor with this binding name, it was not used. No values were generated for it.

This is how activities are expected to work when they are implemented correctly. This means that the bindings themselves are templates for data generation, only to be used when necessary. This means that the bindings that are defined around a statement are more like a menu for the statement. If the statement uses those bindings with `{named}` anchors, then the recipes will be used to construct data when that statement is selected for a specific cycle. The cycle number both selects the statement (via the op sequence) and also provides the input value at the left side of the binding functions.

