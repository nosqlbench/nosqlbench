---
title: Random Data
weight: 5
---

# Random Data

This section touches on topics of using randomized data within NoSQLBench tests.

## Benefits

The benefits of using procedural generation for the purposes of load testing is taken as granted in
this section. For a more thorough discussion on the assumed merits, please see _Showcase_, _Virtual Datasets_
section.

## Basic Theory

In NoSQLBench, the data used for each operation is generated on the fly. However, the data is also deterministic
by default. That means, for a given activity, any numbered cycle will produce the same operation from test to test,
so long as the parameters are the same.

NoSQLBench runs each activity over a specific range of cycles. Each cycle is based on a specific number
from the cycle range. This cycle number is used as the seed value for that cycle. It determines not
only which operation is selected, but also what data is generated and bound to that operation for execution.
The data generation is initialized at the start, and optimized for rapid access during steady state operation.

This is by-design. However, there are ways of selecting how much variation you have from one test scenario to another.

## Managing Variation

Sometimes you will want to run the same test with the same operations, access patterns, and data.
For certain types of testing and comparisons, this is the only way to shed a light on a specific
issue, or variation in performance. The ability to run the same test between different target systems
is extremely valuable.

### Selecting Cycles

You can cause an activity to run a different set of operations simply by changing the cycle range used
in the test.

For an activity that is configured with `cycles=100M`, 100 million independent cycles will be used.
These cycles will be automatically apportioned to the client threads as needed until they are all
used up.

If you want to run 100 million different cycles, all you have to do is specify a different set
of seeds. This is as simple as specifying `cycles=100M..200M`, as the first example above is only short-hand
for `cycles=0..100M`.

### Selecting Bindings

The built-in workloads come with bindings which support the "rampup" and "main" phases appropriately. This means that the cycles for rampup will use a binding that lays data into a dataset incrementally, as you would build a log cabin. Each cycle adds to the data. The bindings are chosen for this effect so that the rampup phase is incremental with the cycle value.

The main phase is selected differently. In the main phase, you don't want to address over the data in order. To emulate a real workload, you need to select the data pseudo-randomly so that storage devices don't get to cheat with read-ahead (more than they would realistically) and so on. That means that the main phase bindings are also specifically chosen for the "random" access patterns that you might expect in some workloads.

The distinction between these two types of bindings should tell you something about the binding capabilities. You can really do what ever you want as long as you can stitch the right functions together to get there. Although the data produced by some of the functions (like `Hash()` for example) look random, it is not. It is, however, effectively random enough for most distributed systems performance testing.

If you need to add randomization to fields, it doesn't hurt to add an additional `Hash()` to the front. Just be advised that the same constructions from one binding recipe to the next will yield the same outputs, so season to taste.



