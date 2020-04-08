---
title: Activity Parameters
weight: 05
---

# Activity Parameters

Activity parameters are passed as named arguments for an activity,
either on the command line or via a scenario script. On the command
line, these take the form of

    <paramname>=<paramvalue>

Some activity parameters are universal in that they can be used with any
driver type. These parameters are recognized by nosqlbench whether or
not they are recognized by a particular driver implementation. These are
called _core parameters_. Only core activity parameters are documented
here.

:::info
To see what activity parameters are valid for a given activity type, see the documentation for that activity type with
`nb help <activity type>`.
:::

When starting out, you want to familiarize yourself with these parameters. The most important ones to learn about first
are driver, cycles and threads.

## driver

For historic reasons, you can also use `type`. They both mean the same
thing for now, but `driver` is more descriptive. The `type` parameter
will continue to be supported in this major version (3.x), but it will
be an error to use it in 4.x and newer.

- `driver=<activity type>`
- _default_: inferred from `alias` or `yaml` parameters, or unset
- _required_: yes, unless inferred
- _dynamic_: no

Every activity is powered by a named ActivityType. Thus, you must set
the `type` parameter. If you do not specify this parameter, it will be
inferred from a substring match against the alias and/or yaml
parameters. If there is more than one valid match for a valid type
value, then you must set the type parameter directly.

Telling nosqlbench what type of an activity will be run also determines
what other parameters are considered valid and how they will be used. So
in this way, the type parameter is actually the base parameter for any
activity. When used with scenario commands like `run` or `start`, an
activity of the named type will be initialized, and then further
activity parameters on the command line will be used to configure it
before it is started.

## alias

- `alias=<alias>`
- _default_: inferred from yaml, or 'UNSET'
- _required_: no
- _dynamic_: no

You *should* set the _alias_ parameter when you have multiple activities,
when you want to name metrics per-activity, or when you want to control
activities via scripting.

Each activity can be given a symbolic name known as an _alias_. It is
good practice to give all your activities an alias, since this
determines the named used in logging, metrics, and even scripting
control.

_default value_ : The name of any provided YAML filename is used as the
basis for the default alias. Otherwise, the activity type name is used.
This is a convenience for simple test scenarios only.

## threads

- `threads=<threads>`
- _default_: 1
- _required_: no
- _dynamic_: yes

You *should* set the _threads_ parameter when you need to ramp up a
workload.

Each activity can be created with a number of threads. It is important
to adjust this setting to the system types used by nosqlbench.

_default value_ : For now, the default is simply *1*. Users must be
aware of this setting and adjust it to a reasonable value for their
workloads.

:::info
The threads parameter will work slightly differently for activities using the async parameter. For example, when
`async=500` is provided, then the number of async operations is split between all configured threads, and each thread
will juggle a number of in-flight operations asynchronously. Without the async parameter, threads determines the logical
concurrency level of nosqlbench in the classic 'request-per-thread' mode. Neither mode is strictly correct, and both
modes can be used for more accurate testing depending on the constraints of your environment.
:::

A good rule of thumb for setting threads for maximum effect is to set it
relatively high, such as 10XvCPU when running synchronous workloads
(when not providing the async parameter), and to 5XvCPU for all async
workloads. Variation in system dynamics make it difficult to peg an
ideal number, so experimentation is encouraged while you dial in your
settings initially.

## cycles

- `cycles=<cycle count>`
- `cycles=<cycle min>..<cycle max>`
- _default_: same as `stride`
- _required_: no
- _dynamic_: no

The cycles parameter determines the starting and ending point for an
activity. It determines the range of values which will act as seed
values for each operation. For each cycle of the test, a statement is
built from a statement template and executed as an operation.

If you do not set the cycles parameter, then it will automatically be
set to the size of the sequence. The sequence is simply the length of
the op sequence that is constructed from the active statements and
ratios in your activity YAML.

You *should* set the cycles for every activity except for schema-like
activities, or activities which you run just as a sanity check of active
statements.

In the `cycles=<cycle count>` version, the count indicates the total
number of cycles, and is equivalent to `cycles=0..<cycle max>`. In both
cases, the max value is not the actual number of the last cycle. This is
because all cycle parameters define a closed-open interval. In other
words, the minimum value is either zero by default or the specified
minimum value, but the maximum value is the first value *not* included
in the interval. This means that you can easily stack intervals over
subsequent runs while knowing that you will cover all logical cycles
without gaps or duplicates. For example, given `cycles=1000` and then
`cycles=1000..2000`, and then `cycles=2000..5K`, you know that all
cycles between 0 (inclusive) and 5000 (exclusive) have been specified.

## stride
- `stride=<stride>`
- _default_: same as op sequence length
- _required_: no
- _dynamic_: no

Usually, you don't want to provide a setting for stride, but it is still
important to understand what it does. Within nosqlbench, each time a
thread needs to allocate a set of cycles to operate on, it takes a
contiguous range of values from a shared atomic value. Thus, the stride
is the unit of micro-batching within nosqlbench. It also means that you
can use stride to optimize a workload by setting the value higher than
the default. For example if you are running a single-statement workload
at a very high rate, it doesn't make sense for threads to allocate one
op at a time from a shared atomic value. You can simply set
`stride=1000` to cause (ballpark estimation) about 1000X less internal
contention.

The stride is initialized to the calculated sequence length. The
sequence length is simply the number of operations in the op sequence
that is planned from your active statements and their ratios.

:::info
When simulating multi-op access patterns in non-async mode, the
stride metric can tell you how long it took for a whole group of
operations to complete.
:::

## async

- `async=<ops>`
- _default_: unset
- _required_: no
- _dynamic_: no

The `async=<ops>` parameter puts an activity into an asynchronous
dispatch mode and configures each thread to juggle a proportion of the
operations specified. If you specify `async=500 threads=10`, then each
of 10 threads will manage execution of 50 operations at a time. With
async mode, a thread will always prepare and send operations if there
are fewer in flight than it is allotted before servicing any pending
responses.

Async mode also puts threads into a different sequencing behavior. When
in async mode, responses from an operation may arrive in a different
order than they are sent, and thus linearized operations can't be
guaranteed as with the non-async mode. This means that sometimes you use
want to avoid async mode when you are intentionally simulating access
patterns with multiple linearized operations per user as you may see in
your application.

The absence of the async parameter leaves the activity in the default
non-async mode, where each thread works through a sequence of ops one
operation at a time.

## cyclerate

- `cyclerate=<cycle_per_second>`
- `cyclerate=<cycles_per_second>,<burst_ratio>`
- _default_: unset
- _required_: no
- _dynamic_: yes

The cyclerate parameter sets a maximum op rate for individual cycles
within the activity, across the whole activity, irrespective of how many
threads are active.

:::info
The cyclerate is a rate limiter, and can thus only throttle an activity
to be slower than it would otherwise run. Rate limiting is also an
invasive element in a workload, and will always come at a cost. For
extremely high throughput testing, consider carefully whether your
testing would benefit more from concurrency-based throttling as with
async or the striderate described below.
:::

When the cyclerate parameter is provided, two additional metrics are
tracked: the wait time and the response time. See the 'Reference|Timing
Terms' section for more details on these metrics.

_default_: None. When the cyclerate parameter is not provided, an
activity runs as fast as it can given how fast operations can complete.

Examples:
- `cyclerate=1000` - set the cycle rate limiter to 1000 ops/s and a
  default burst ratio of 1.1.
- `cyclerate=1000,1.0` - same as above, but with burstrate set to 1.0
  (use it or lose it, not usually desired)
- `cyclerate=1000,1.5` - same as above, with burst rate set to 1.5 (aka
  50% burst allowed)

### burst ratio

This is only an optional part of the cyclerate as shown in examples
above. If you do not specify it when you initialize a cyclerate, then it
defaults 1.1. The burst ratio is only valid as part of a rate limit and
can not be specified by itself.

* _default_: `1.1`
* _dynamic_: yes

The nosqlbench rate limiter provides a sliding scale between strict rate
limiting and average rate limiting. The difference between them is
controlled by a _burst ratio_ parameter. When the burst ratio is 1.0
(burst up to 100% relative rate), the rate limiter acts as a strict rate
limiter, disallowing faster operations from using time that was
previously forfeited by prior slower operations. This is a "use it or
lose it" mode that means things like GC events can steal throughput from
a running client as a necessary effect of losing time in a strict timing
sense.

When the burst ratio is set to higher than 1.0, faster operations may
recover lost time from previously slower operations. For example, a
burst ratio of 1.3 means that the rate limiter will allow bursting up to
130% of the base rate, but only until the average rate is back to 100%
relative speed. This means that any valleys created in the actual op
rate of the client can be converted into plateaus of throughput above
the strict rate, but only at a speed that fits within (op rate * burst
ratio). This allows for workloads to approximate the average target rate
over time, with controllable bursting rates. This ability allows for
near-strict behavior while allowing clients to still track truer to rate
limit expectations, so long as the overall workload is not saturating
resources.

:::info
The default burst ratio of 1.1 makes testing results slightly more stable on average, but can also hide some
short-term slow-downs in system throughput. It is set at the default to fit most tester's expectations for averaging
results, but it may not be strict enough for your testing purposes. However, a strict setting of 1.0 nearly always adds
cold/startup time to the result, so if you are testing for steady state, be sure to account for this across test runs.
:::

## striderate

- `striderate=<strides per second>`
- `striderate=<strides per second>,<burst_ratio>`
- _default_: unset
- _required_: no
- _dynamic_: yes

The `striderate` parameter allows you to limit the start of a stride
according to some rate. This works almost exactly like the cyclerate
parameter, except that it blocks a whole group of operations from
starting instead of a single operation. The striderate can use a burst
ratio just as the cyclerate.

This sets the target rate for strides. In nosqlbench, a stride is a group of
operations that are dispatched and executed together within the same thread.
This is useful, for example, to emulate application behaviors in which some
outside request translates to multiple internal requests. It is also a way
to optimize a client runtime for more efficiency and throughput. The stride
rate limiter applies to the whole activity irrespective of how many threads
it has.

:::warning
When using the cyclerate an striderate options together, operations are delayed based on both rate limiters. If the
relative rates are not synchronised with the side of a stride, then one rate limiter will artificially throttle the
other. Thus, it usually doesn't make sense to use both of these settings in the same activity.
:::

## seq

- `seq=<bucket|concat|interval>`
- _default_: `seq=bucket`
- _required_: no
- _dynamic_: no

The `seq=<bucket|concat|interval>` parameter determines the type of
sequencing that will be used to plan the op sequence. The op sequence is
a look-up-table that is used for each stride to pick statement forms
according to the cycle offset. It is simply the sequence of statements
from your YAML that will be executed, but in a pre-planned, and highly
efficient form.

An op sequence is planned for every activity. With the default ratio on
every statement as 1, and the default bucket scheme, the basic result is
that each active statement will occur once in the order specified. Once
you start adding ratios to statements, the most obvious thing that you
might expect wil happen: those statements will occur multiple times to
meet their ratio in the op mix. You can customize the op mix further by
changing the seq parameter to concat or interval.

:::info
The op sequence is a look up table of statement templates, *not*
individual statements or operations. Thus, the cycle still determines
the uniqueness of an operation as you would expect. For example, if
statement form ABC occurs 3x per sequence because you set its ratio to
3, then each of these would manifest as a distinct operation with fields
determined by distinct cycle values.
:::

There are three schemes to pick from:

### bucket

This is a round robin planner which draws operations from buckets in
circular fashion, removing each bucket as it is exhausted. For example,
the ratios A:4, B:2, C:1 would yield the sequence A B C A B A A. The
ratios A:1, B5 would yield the sequence A B B B B B.

### concat

This simply takes each statement template as it occurs in order and
duplicates it in place to achieve the ratio. The ratios above (A:4, B:2,
C:1) would yield the sequence A A A A B B C for the concat sequencer.

### interval

This is arguably the most complex sequencer. It takes each ratio as a
frequency over a unit interval of time, and apportions the associated
operation to occur evenly over that time. When two operations would be
assigned the same time, then the order of appearance establishes
precedence. In other words, statements appearing first win ties for the
same time slot. The ratios A:4 B:2 C:1 would yield the sequence A B C A
A B A. This occurs because, over the unit interval (0.0,1.0), A is
assigned the positions `A: 0.0, 0.25, 0.5, 0.75`, B is assigned the
positions `B: 0.0, 0.5`, and C is assigned position `C: 0.0`. These
offsets are all sorted with a position-stable sort, and then the
associated ops are taken as the order.

In detail, the rendering appears as `0.0(A), 0.0(B), 0.0(C), 0.25(A),
0.5(A), 0.5(B), 0.75(A)`, which yields `A B C A A B A` as the op
sequence.

This sequencer is most useful when you want a stable ordering of
operation from a rich mix of statement types, where each operations is
spaced as evenly as possible over time, and where it is not important to
control the cycle-by-cycle sequencing of statements.


