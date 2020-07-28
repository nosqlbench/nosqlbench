---
title: Multiple Clients
weight: 9
---

# Multiple Clients (Q&A)

This page is a basic FAQ regarding multiple clients with NoSQLBench.
The details in this section will be absorbed into the docs unless users find this format more useful. (Please give feedback on the Q&A format!)

-----

**question**

What is the right approach to run multiple instances of NoSQLBench (nb) for a given test?

**answer**

NoSQLBench can generate a significant amount of traffic. If you are testing with more than 5 nodes on the server side (for comparable hardware) then it may be necessary to add more clients if you are indeed wanting to generate a saturating workload. Otherwise, one client is nearly always enough. Of course, you may want to double check the resource usage on your client and then decide. Generally speaking, if your CPU is over 50% on the client, then it's a good idea to add more clients.

If you need to add more clients, then you can make sure they are using different
data and thus splitting the workload by ensuring that they each operate on a
different set of cyles. For example, with a total workload size of 100M cycles,
you can split it by setting `cycles=0..50M` on the first client, and then `cycles=50M..100M` on the second. This approach can be used to split cycles among any number of clients.

-----

**question**

I observed that nb is creating exactly the same transactions each time it is run. I do understand that this is a feature and not a bug and supports reproducibly. I thought I could run nb from multiple drivers, but then I would need a more randomized behavior in nb.

**answer**

Yes,  The cycle range used in the test actually changes the data used in the data bindings. If you are generating pseudo-random data already, you can simply use a different cycle range. For example cycles=100M (shorthand for cycles=0..100M) is one set of operations, and cycles=100M..200M is a different set of operations (also 100M total, but different values are used within the operations)

This is a common enough request that we are going to add a way to hash the
input different for different tests when desired. This will not be applied
by default, but when needed it will become the easiest way to handle this type of scenario.

-----

**question**

Is there a more verbose documentation on the syntax of the yaml files that describe the benchmark. A list of examples would be welcome, too.

**answer**

The section of the docs called "Building Workloads" is actually a detailed
explanation of the yaml format. The YAML format and the concepts that
one needs to understand it are woven together here with detailed examples
from start to end.

-----

**question**

I installed ops center and used it to visualize metrics such as Read Requests, Read Request Latency, OS: CPU and others. This works.

I also tried --docker-metrics on the nb command line. I was able to open Grafana on port 3000 and found some metrics, but not Read Request, Write Request etc. It seems to be that ops center has more information.

**answer**

The metrics recorded by NoSQLBench are client-side. OpsCenter looks at server-side metrics. You can have both in one place if you use dsemetricscollector and combine the configs, but it is not as easy as just using --docker-metrics. We will add better docs for this.

When lookin at metrics, it is critical that you know what the vantage point is for each one, and what it means for the test results. A new section has been added to this section of the docs called "Vantage Points" as a primer for this.

There are generally 4 vantage points used of some significance in C* testing:

1. Application (same as nb in this case)
2. Driver/Data Layer (generally the same as nb in this case, but we do offer driver metrics separately if needed)
3. Coordinator (sometimes called Proxy)
4. Replica

The latter 2 are the only ones you will see in OpsCenter. It usually makes sense to look at the path and do some deduction about the differences, say the difference in read latency from the client, proxy, or storage levels.


-----

**question**

I also checked the metrics at end of the log file that are created by nb and didn’t find a breakdown into read/write metrics either. I used Cassandra-stress in the past and remember that it provided such information in their log file.

**answer**

If you want to instrument your statements in nosqblench so that metrics are provided separately for each statement, you can do that by throwing the `instrument: true` option on your statements in the yaml. This works for the CQL driver and we will look at ways to support it in other drivers too.

-----

**question**

I’m looking for something that can be scripted so that I can run multiple variations and extract results automatically.

**answer**

NoSQLBench can definitely do that. It is what it was built for. For multiple variations either use the cycle range setting as described above, or add a permutation function to the head of your binding recipes.

Getting down to the details of what you mean by "variation" might be a quick conversation, but it could also be in-depth depending on your requirements. For simple cases, just throwing a Hash() into the front will cause the data to be randomized. You can also consider the Shuffle(...) functions with different bank numbers.

The feature mentioned above for pre-hashing will be the easiest way to do this once it is implemented.
