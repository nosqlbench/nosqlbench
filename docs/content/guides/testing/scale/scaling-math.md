+++
title = "Scaling Math"
description = "Understanding the mathematics of scaling distributed systems"
weight = 30
template = "page.html"

[extra]
quadrant = "guides"
topic = "testing"
category = "scale"
tags = ["scaling", "performance", "mathematics", "extrapolation"]
+++

## Scaling Trade-Offs

In general, there is a triangular trade-off between service time, op rate, and data density,
where reads (index traversals or "lookups") are generally more dependent on working set size than
other operations. There is a fundamental three-way trade-off between higher throughput, higher
working set, and lower (better) latency. This is true for all modern databases. In general, read
patterns always access indexes of some type for a random-access system. The performance of these
indexes varies widely based on hardware, software, and data factors. Ideally, index performance
tends to Ω(Log₂(n)) for binary search, which approximates the best performance
possible, except in special data-dependent cases, where slightly improved performance is
possible, but not generally reliable. No matter the underlying hardware, the cardinality of
your indexable values *will* be a factor.

This is important to keep in mind, since it makes it very clear that you must test with
realistic data -- enough data with enough variety and a characteristic distribution. However,
you don't have to do this at the same scale as the system you are characterizing for.

The data or index density question can be addressed per unit, meaning per-node, per-core, or
whatever the fundamental unit of deployed scale your system architecture offers.

## Test small and extrapolate

It is not necessary nor reasonable to test every system at production scale for the purposes of
trusting its operational behavior. The basic principles used to build scalable systems allow for
us to build _scale_ models of these systems and verify the character of scaling itself. Nearly
all scalable systems amortize work over time and space. There is always a per-unit way of
measuring capacity such that the projected capacity is directly proportional to the scale of the
system deployment. Identifying how the system capacity relates to the deployment footprint is
essential, and depends on the primary scaling mechanisms of the system in question.

You want to be able to make reliable statements about the scale of some supposed system
deployment from the data you collect in the smaller-scale test, like

1. If I add more data to the existing system, how does this manifest in terms of throughput or
   latency impact?
2. If I were to add more resources to the system, does it scale up linearly or is there otherwise a
   reliable estimate which can be used?
3. If a unit of scale is removed (like a node), what happens to the throughput and latency?
4. For a given increase in system resources across the board, how are the throughput and
   latency affected?

To answer these questions, you need to establish some basic formulae and verify them. What this
means will depend directly on your system design.

# Test the extrapolation

Once you have established the formulae to estimate changes in capacity or performance based on
changes in system topology, you need change the scale of the system and run your test again to
verify that the character of scaling holds. If it does not, then there is an important detail
that needs to be discovered and added to the scaling math.

# Trust your extrapolation

Once you've proven that your scaling math works, it's time to capture it in context. You need to
document the details of the test, including workload, dataset, density, topology, system
configuration, and so on. It is only by knowing how similar this system is to another supposed
system that you can trust the data for other estimates.

A frequent mistake that technologists make when testing systems like this is using the data out
of context, against a system where only a couple details have changed. While this might sound
reasonable, once you go beyond two or three small changes, our ability to reliably predict
systemic changes drops of very quickly. More advanced analysis might help to make this test data
more portable, but it is certainly a fools errand to try to intuit too far away from your known
configuration.

By knowing when a system is congruent to the one you tested, you can known when to trust your
scaling math. This is why it is critical that you document the circumstances of what and how you
tested, so that in future situations you know if you have meaningfully relevant data.


Once you have the ability to extrapolate or interpolate (within reason) how system topology
affect the operational behavior of your system, it's time contextualize your data. If is fair to
use the scaling math for other systems which are the same as the one you tested.
