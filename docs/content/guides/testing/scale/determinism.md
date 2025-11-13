+++
title = "Determinism & Variation"
description = "Using procedural generation for reproducible tests"
weight = 10
template = "page.html"

[extra]
quadrant = "guides"
topic = "testing"
category = "scale"
tags = ["determinism", "data-generation", "reproducibility", "testing"]
+++

This section touches on topics of using randomized data within NoSQLBench tests.

## Benefits

The benefits of using procedural generation for the purposes of load testing is taken as granted in
this section. For a more thorough discussion on the merits, please see the Virtual DataSet
concepts in the explanations section.

## Basic Theory

In NoSQLBench, the data used for each operation is generated on the fly. However, the data is also
deterministic by default. That means, for a given activity, any numbered cycle will produce the same
operation from test to test, so long as the parameters are the same.

NoSQLBench runs each activity over a specific range of cycles. For each cycle, an operation is
built from a template using that cycle as a seed. The cycle value determines which op template
to use as well as all the payload values and/or configuration details for that operation.

The machinery which is responsible for dispatching operations to run is initialized before an
activity starts to optimize performance while the activity is running.

## Managing Variation

There are ways of selecting how much variation you have from one test scenario to another.

Sometimes you will want to run the same test with the same operations, access patterns, and data.
This may be necessary in advanced testing scenarios, since it can make data or order-specific
problems reproducible. The ability to run the same test between different target systems is
extremely valuable.

### Selecting Cycles

You can cause an activity to run a different set of operations simply by changing the cycle range
used in the test.

For an activity that is configured with `cycles=100M`, 100 million independent cycles will be used.
These cycles will be automatically apportioned to the client threads as needed until they are all
used up.

If you want to run 100 million different cycles, all you have to do is specify a different set of
seeds. This is as simple as specifying `cycles=100M..200M`, as the first example above is only
short-hand for `cycles=0..100M`.

### Selecting Bindings

The built-in workloads come with bindings which support the _rampup_ and _main_ testing activities
appropriately. This means that the cycles for rampup will use a binding that lays data into a
dataset incrementally, as you would build a log cabin. Each cycle adds to the data. The bindings are
chosen for this effect so that the rampup activity is incremental with the cycle value.

The main activity bindings are selected differently. In the main activity, you don't want to
address over the data in order. To emulate a real workload, you need to select the data
pseudo-randomly so that storage devices don't get to cheat with read-ahead (more than they
would realistically) and so on. That means that the main activity bindings are also specifically
chosen for the "random" access patterns that you might expect in some workloads.

The distinction between these two types of bindings should tell you something about the binding
capabilities. You can really do what ever you want as long as you can stitch the right functions
together to get there. Although the data produced by some functions (like `Hash()` for
example) look random, they are not. They are, however, effectively random enough for most
distributed systems performance testing.

If you need to add randomization to fields, it doesn't hurt to add an additional `Hash()` to the
front. Just be advised that the same constructions from one bind point to the next will yield
the same outputs, so season to taste.
