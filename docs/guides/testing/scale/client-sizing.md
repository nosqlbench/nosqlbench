+++
title = "Client Sizing"
description = "How to size your test clients to avoid bottlenecks"
weight = 20
template = "docs-page.html"

[extra]
quadrant = "guides"
topic = "testing"
category = "scale"
tags = ["scaling", "client-sizing", "performance", "testing"]
+++

How do you know what kind of client to run? How do you know how many clients to run? How do you see
the result of running multiple clients? We attempt to answer these questions below.

## Testing Asymmetry

When you are measuring system performance, certain precautions have to be taken in order to ensure
that you are measuring what you intend. One of the most fundamental requirements is that the systems
that generate load and measure results must be inherently more capable of scale than the system you
are measuring. In other words, if the testing system is the bottle-neck in the composed system, then
you are effectively measuring your testing system and not the target.

This is a sliding scale. For instance, if your testing system's resources are _mostly_ saturated,
say "80% utilized", then you are likely still leaving some performance untapped, or at the very
least, measuring higher response times than you would otherwise. This follows from the fact that
clients are not real-time systems and must juggle threads and other parallel computing resources in
order to service results from the target system. There is no simple or practical way to avoid this.

So, in order to have empirical results which are accurate with respect to the target of your tests,
your client resources must not be over-utilized. In most cases, a client which uses less than 60% of
its CPU and is not otherwise throttled by resource contention will provide generally accurate
results.

In some testing systems, such as those which pipe around data in order to replay operations, you
will find that it is more difficult to scale your client nodes up than with NoSQLBench. That is
because moving serialized operations around and then consuming them in real time is simply much more
work on the client system. This forces you into a situation where the load bank needs to be much
larger to offset processing demands in the testing apparatus. Methods involving local IO or
pre-processing will generally be much slower than those which operate almost entirely within the
CPU-Memory complex. NoSQLBench's approach to synthesizing operations from recipes avoids much of
this concern, or at least makes it easier to manage and scale.

Yet, the amount of client capability you need in order to run an accurate performance test still
depend on how capable your test target is. Consider a target cluster comprised of 5 nodes. This may
take a couple of test clients _of the same basic hardware profile_ in order to adequately load the
target system without saturating client resources. However, if those clients are saturating
their CPUs, this is not enough.

### Verify client overhead

When running serious tests, it is important that you look at your clients' system utilization
metrics. The key metric to watch for is CPU utilization. If it goes over 50%, you may need more
client resources in order to keep your metrics accurate. There are a couple ways to approach this.

### Use a larger test node

If you need to scale up your test client capabilities, the simplest method is to just use a larger
client node. This may also require you to increase the number of threads, but
`threads=auto` will always size up reasonably to the available cores.

### Add more client nodes

If you need, you can add more nodes to your test. There are a couple of strategies for doing this
effectively.

1. If you want to run the same number of operations overall, you can split them over nodes. Simply
   change your `cycles` activity param so that it allocates a share of the cycles to each node. For
   example, a single-node test which uses `cycles=1M` can be split into two different ones which
   use `cycles=500K` and `cycles=500k..1M`. If you instead simply set the number to
   `500k` for each of them, you would be running the same exact operations twice.
2. If you are gathering metrics on a dashboard, you will want to alias the workload name. If you are
   using `Named Scenarios` as most users do, then you can simply copy the workload name to another.
   This allows you to receive distinct metrics by client. When tagged metrics are implemented, this
   will no longer be necessary.

A good rule of thumb to use is 1 client node for each 3 target nodes in a cluster. This will
of-course vary by workload and hardware, but it is a reasonable starting point.

It is also important to consider the section on [Scaling Math](scaling-math.md)
when sizing your load bank.
