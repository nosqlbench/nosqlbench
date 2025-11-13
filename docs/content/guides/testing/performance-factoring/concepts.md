+++
title = "Performance Concepts"
description = "Fundamental concepts for performance characterization"
weight = 10
template = "page.html"

[extra]
quadrant = "guides"
topic = "testing"
category = "analysis"
tags = ["performance", "concepts", "methodology", "analysis"]
+++

# Key Terms

## Operational Systems

When a system has no headroom, it is unable to be responsive during hardware or infrastructure
outages, load spikes, changes in system topology, capacity scaling activities, other
administrative activities like external backups.

User-facing systems are more often operational systems, which require a degree of built-in
headroom in order to absorb load spikes or events described above. These systems are designed
and tuned more for operational responsiveness and resiliency under load and workload changes.
They are also combined with other less-operational designs as a fronting layer to absorb and
manage around limitations in classic system designs.

Measuring the capacity of operational system without putting its operational headroom and
responsiveness in focus is usually meaningless. We often measure the capacity of such systems as
a _reference point_ within which other, more realistic measurements may be taken.

## Saturating Throughput

The throughput at which a system is fully utilizing its most critical resource and can go no
faster. This is a measure of how much work a system is capable of. When a system is saturated,
you will see the highest response times. This is typically measured in ops per second, sometimes
abbreviated as "ops/s", "Kops/s", or just simply "Kops".

## Operational Headroom

At saturating throughput, a system has no operational headroom. Operational headroom is the
fraction of system capacity (throughput) which remains in reserve to handle load spikes,
topology changes, hardware failures, external backup loads, and so on.

## Nominal Throughput

A production-like workload in an operational system is one which takes into account the
operational headroom that it would normally be run with in production. This is absolutely
essential in how findings are presented so that users and operators make safe choices about
system sizing, tuning, and so on. Case in point: If you ran a cluster at 100% of its saturating
capacity, it would not be able to absorb a node outage without affecting the load. The load
(user-facing workload, ostensibly) would be compromised in some way, from a lowered aggregate
capacity, which would also likely result in significantly higher response times. Systems which
are affected by over-saturation in this way are often much more severely affected than new
operators would expect, due to the combination of service time and capacity effects stacking in
an unhealthy way. This is easy to demonstrate in practice, and made more lucid through the lens
of [Little's law](https://en.wikipedia.org/wiki/Little%27s_law).

Thus, a key insight about operational systems is that they need to be deployed and tested
with _operational headroom_ as explained above. A typical approach for testing a distributed
system with built-in fault management capabilities would be to characterize its saturation
throughput and then factor in headroom depending on the topology of the system. For a 5 node
cluster, 70% throughput is a reasonable starting point.


## End-to-End Testing

To accurately gauge how a system performs (operationally) or behaves (functionally), you can
either test it piece-wise -- subsystem by subsystem, or you can test it fully assembled in an
integrated fashion. An end-to-end test takes this further to include the composed system from
the end-user, access pathways and infrastructure, and all internal services or endpoints that
the system under test may depend on. End-to-end testing comes with a few key trade-offs:

1. End-to-end testing provides the most coverage of any type of test. As such, it is often one
   of the first ways an operational system is tested in order to determine whether the system as
   a whole is operating and functioning correctly.
2. End-to-end testing does not provide a destructured of the system. It doesn't focus the
   user's on the  element which may be causing a test failure. Further analysis is required to
   figure out why errors occur.
   - In the very best system designs, failure modes manifest with specific reasons
      for why an error occurred, and specific details about where in the system it occurred. This
      does not carry over to performance testing very well.
   - Further, many systems intentionally obscure internal details for reasons of security or
     decoupling user experience from system details. This is common with cloud services for example.
3. End-to-end testing is a form of integration testing, where potentially large deployments or
   configurations are required in order to facilitate testing activities. This leaves a larger
   surface area for misconfiguration which is incongruent with prod or customer specific details.
   Where these deployment manifests may diverge from actual customer systems, results may be
   invalid, in the form of false-positives or false-negatives.
4. Testing end-to-end on real systems is feasible with the right supporting test apparatus and
   path addressing methods, such as multi-tenant features of a system. However,
   doing this reliably and accurately requires the provisioning and auditing logic for testing
   systems to be one and the same as what customers or operators use in order to ensure that
   there is no difference between what is tested and what is intended to be tested.
5. Physical topology and logical pathing are not always visible or explicitly addressable from
   the testing apparatus, meaning that testing a whole system across all flows may be impossible
   without special routing or flagging logic in testing operations. If these are not
   in-built mechanisms in the user-facing functionality, then it creates a side-path which
   breaks system coverage guarantees. This makes some end-to-end test scenarios probabilistic,
   in that physical traversal paths may only be hit a fraction of the time.

## Scaling Math

In distributed systems testing, a common misconception is that you have to test your production
loads at the same level as you would run them in production. This flies in the face of the
design principles of linearly scalable systems. What is more important is that the effects of
proportionality and congruence hold, in a durable way, so that observations on smaller systems
can be used to make reliable predictions of larger systems. The methods used to do this are
often described as _scaling math_ in the NoSQLBench user community.

For example, suppose you have a workload that supports a line of business, a peak capacity
requirement. With a provable presumption of linear scalability over nodes, you can do a test with 5
nodes which will provide the baselines measurements needed to project node requirements for any
given throughput. This example doesn't include the latency, but it can be extended to that once the
throughput requirements are understood.

In fact, the principles and specific mechanisms of scale should be the key focus of any such
studies. Once these are established as trust-worthy, further predictions or analysis become much
easier.

Detailed examples with latency factors included will be given elsewhere.
