+++
title = "Key Workflows"
description = "Common performance testing workflows and methodologies"
weight = 20
template = "page.html"

[extra]
quadrant = "guides"
topic = "testing"
category = "analysis"
tags = ["performance", "workflows", "methodology", "testing"]
+++

There are a few workflows which we see routinely in performance testing. These workflows capture,
or at least describe the steps in a complete analysis method, and are the basis for extant analysis
method
scripts in NoSQLBench. This section describes some of these, the steps involved, and the purposes of
each step.

On the surface, many of these techniques may appear to be pretty basic. Descriptions like "find
the highest throughput" are an egregious misnomer for what is required to do this type of work
accurately and precisely. This is important for everyone to understand who depends on the
results of performance testing. Over-simplifying or rushing past these details can effectively
invalidate the results to the point of being less useful than guesswork, particularly if the
stakeholders presume a degree of methodical approach or empiricism which is not present.

# System Preparation

1. Deploy Testing apparatus, infrastructure, and target systems.
2. Document deployment details, including steps needed to redeploy the same type of system with
   the same provisioning levels, settings, topology, etc.

Once a system is deployed for testing, all the essential details of the system which define
the test scenario should be captured.

Empirically, this is every detail of the system, but
practically speaking this is often not possible or convenient for these details to be capture.
A key strategy is to use well-defined reference points, like a system image, default
configuration, hardware profile, etc., and then document only deltas from this initial state.
This also emphasizes the value of testing system as shipped or configured by default, since this
is also a meaningful reference point for any other analysis.

# Measure Saturating Throughput

To balance and inform how performance data is interpreted, it is crucial to consider the
throughput and response (AKA latency) in contrast and in combination. In order to do this well,
it is essential to determine the maximum rate at which a system can process requests for the
workload under study. This is what _measuring saturating throughput_ is all about.

The saturating throughput for a composed system may be limited by any component, including
clients, infrastructure, servers or endpoints, proxy layers, storage subsystems, etc.
By definition, it *is* dependent on each and every part of the composed system. However, it is
almost always more dependent on one component than others. This is often called a _bottleneck._
When a given component is disproportionately utilized over others, the system is may not be considered
well-balanced. When this component hits full saturation, limiting the throughput of the composed
system, then it is called a bottleneck. It is not always easy to define what constitutes a
meaningful bottleneck when resource utilization is relatively even. Over-tuning for full
saturation can be counterproductive.

It is essential to understand the general state of balance of a system at saturation. If
optimizing for throughput, then key metrics should include any skews in workload distribution
over nodes, resources, or services. As well, within vertical resource profiles, such as within a
node, serious imbalances may invalidate the purposes of a test. This all depends on the specific
reason for running the test.

Yet, it is possible, in a well-balanced system, that many components are _highly_ saturated
together. In many cases, this a desired state of balance. In a well-balanced system,
appreciable speed-ups require creating more headroom (scaling up capacity) in all the components
or subsystems. Once this is achieved in vertical resource profiles, simpler scale-out
strategies become available, wherein you know each unit of capacity is representative of a unit of
consumption for the given workload.


In practice, valid results are only possible when the target being tested is the limiting
factor. Further, as the testing apparatus sees higher utilization (client-side or
infrastructure) , the fidelity of results decreases. The relationship between client saturation
and measurement accuracy is not well-defined, but is nearly always a non-linear relationship.
For example running the client system at 60% utilization will certainly increase the measured
latency of the composed system over the same test rate on a client which would only be 40%
utilized. It is important to remember that whey you are running a test, there is no way to
_only test the server_. However, you can shift the measurements to be more descriptive of the
test target by ensuring that the whole system is over-provisioned in the testing apparatus as a
rule.

## Steps

1. Prepare target system, infrastructure, and client systems.
2. Instrument client system for basic metrics capture, including throughput and discrete latency
   histograms. (Avoid time-decaying or other leavening effects.)
    1. (Advanced) Instrument each key subsystem, messaging layer, system boundary, and resource
       pool in the entire composed system.
    2. (Advanced) Baseline key subsystems for capacity using automated benchmarking tools.
    3. (Advanced) Verify or record performance congruity and coherence across tested systems.
3. Configure workload at sufficient concurrency. Minimum concurrency should keep all
   messaging paths primed at all times (transport, buffering requests, processing elements), with
   minimal over-commit. A good rule of thumb is to set concurrency to 2X estimated operational
   parallelism.
4. Method 1 - Run the workload at the full capacity of the client, adjusting the concurrency to
   find the local maxima in throughput. Adjust settings as need to optimize throughput until
   further improvement is minimal.
5. Method 2 - Run the workload with a rate limiter on the client side as the limiting factor.
   Adjust the rate limit to find the local maxima in throughput. Adjust setting as needed to
   optimize throughput until further improvement is minimal.
6. Method 3 - Use an automated and iterative analysis method like findmax in order to
   streamline the testing time, and codify the analysis method for reproducible and specific
   results.
7. Method 4 - Use an automated and iterative analysis method like optimo in order to genearlize
   over an n-dimensional parameter space which includes concurrency and other dynamic settings.
7. Record the **result: maximum throughput and the settings required to achieve it**.
8. (Advanced) Record the response curve of the system across key throughput stages.

# Measure Ideal Latency

Determine the latency of an operation under the best possible circumstances, i.e. all JIT,
cache-warming is done, indexes and compaction state are optimal, and so on.
