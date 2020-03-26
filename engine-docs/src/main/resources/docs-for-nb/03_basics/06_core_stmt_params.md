---
title: Core Statement Params
weight: 06
---

# Core Statement Parameters

Some statement parameters are recognized by the nosqlbench runtime and can be used on any statement in a YAML file.

## *ratio*

A statement parameter called _ratio_ is supported by every workload. It can be attached to a statement, or a block or a
document level parameter block. It sets the relative ratio of a statement in the op sequence before an activity is
started.

When an activity is initialized, all of the active statements are combined into a sequence based on their relative
ratios. By default, all statement templates are initialized with a ratio of 1 if non is specified by the user.

For example, consider the statements below:

```yaml
statements:
  - s1: "select foo,bar from baz where ..."
    ratio: 1
  - s2: "select bar,baz from foo where ..."
    ratio: 2
  - s3: "select baz,foo from bar where ..."
    ratio: 3
```

If all statements are activated (there is no tag filtering), then the activity will be initialized with a sequence
length of 6. In this case, the relative ratio of statement "s3" will be 50% overall. If you filtered out the first
statement, then the sequence would be 5 operations long. In this case, the relative ratio of statement "s3" would be 60%
overall. It is important to remember that statement ratios are always relative to the total sum of the active
statements' ratios.

:::info
Because the ratio works so closely with the activity parameter `seq`, the description for that parameter is include
below.
:::

### *seq* (activity level - do not use on statements)

- `seq=<bucket|concat|interval>`
- _default_: `seq=bucket`
- _required_: no
- _dynamic_: no

The `seq=<bucket|concat|interval>` parameter determines the type of sequencing that will be used to plan the op
sequence. The op sequence is a look-up-table that is used for each stride to pick statement forms according to the cycle
offset. It is simply the sequence of statements from your YAML that will be executed, but in a pre-planned, and highly
efficient form.

An op sequence is planned for every activity. With the default ratio on every statement as 1, and the default bucket
scheme, the basic result is that each active statement will occur once in the order specified. Once you start adding
ratios to statements, the most obvious thing that you might expect wil happen: those statements will occur multiple
times to meet their ratio in the op mix. You can customize the op mix further by changing the seq parameter to concat or
interval.

:::info
The op sequence is a look up table of statement templates, *not* individual statements or operations. Thus, the cycle
still determines the uniqueness of an operation as you would expect. For example, if statement form ABC occurs 3x per
sequence because you set its ratio to 3, then each of these would manifest as a distinct operation with fields
determined by distinct cycle values.
:::

There are three schemes to pick from:

### bucket

This is a round robin planner which draws operations from buckets in circular fashion, removing each bucket as it is
exhausted. For example, the ratios A:4, B:2, C:1 would yield the sequence A B C A B A A. The ratios A:1, B5 would yield
the sequence A B B B B B.

### concat

This simply takes each statement template as it occurs in order and duplicates it in place to achieve the ratio. The
ratios above (A:4, B:2, C:1) would yield the sequence A A A A B B C for the concat sequencer.

### interval

This is arguably the most complex sequencer. It takes each ratio as a frequency over a unit interval of time, and
apportions the associated operation to occur evenly over that time. When two operations would be assigned the same time,
then the order of appearance establishes precedence. In other words, statements appearing first win ties for the same
time slot. The ratios A:4 B:2 C:1 would yield the sequence A B C A A B A. This occurs because, over the unit interval
(0.0,1.0), A is assigned the positions `A: 0.0, 0.25, 0.5, 0.75`, B is assigned the positions `B: 0.0, 0.5`, and C is
assigned position `C: 0.0`. These offsets are all sorted with a position-stable sort, and then the associated ops are
taken as the order.

In detail, the rendering appears as `0.0(A), 0.0(B), 0.0(C), 0.25(A), 0.5(A), 0.5(B), 0.75(A)`, which yields `A B C A A
B A` as the op sequence.

This sequencer is most useful when you want a stable ordering of operation from a rich mix of statement types, where
each operations is spaced as evenly as possible over time, and where it is not important to control the cycle-by-cycle
sequencing of statements.


