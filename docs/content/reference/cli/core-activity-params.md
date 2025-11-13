+++
title = "Core Activity Parameters"
description = "Universal activity parameters available with all drivers"
weight = 30
template = "page.html"

[extra]
quadrant = "reference"
topic = "cli"
category = "parameters"
tags = ["parameters", "activities", "configuration", "reference"]
+++

Activity parameters are passed as named arguments for an activity, either on the command line
or from a scenario script. On the command line, these take the form of

    ... <param>=<value> ...

Some activity parameters are universal in that they can be used with any driver type. These
parameters are called _core_ activity params. Only core activity parameters are documented here.
When starting out, you want to familiarize yourself with these parameters.

👉 To see what activity parameters are valid for a given driver, see the documentation for that
driver with `nb5 help <driver>`. Each driver comes with documentation that describes what how
to configure it with additional driver params as well as what op template forms it can understand.

# Essential

The activity params described in this section are those which you will use almost all the time
when configuring activities.

👉 If you aren't using one of these options with a `run` or `start` command, or otherwise in your
named scenarios, double check that you aren't missing something important.

## driver

- `driver=<driver>`
- _default_: unset
- _required_: no
- _dynamic_: no

Every activity can have a default driver. If provided, it will be used for any op template which
does not have one directly assigned as an op field. For each op template in the workload, if no
driver is set, an error is thrown.

As each activity can have multiple op templates, and each op template can have its own driver,
the available activity params for a workload are determined by the superset of valid params for
all active drivers.

You can find out what drivers are available in nb5 with the `--list-drivers` option from
[discovery options](options.md#discovery-options). You can then get details on
what each of these drivers allow with `nb5 help <driver>`.

The driver selection for an op template determines the valid constructions for the op template.
For example:

```yaml
# file test.yaml
ops:
  op1:
    op:
      driver: stdout
      stmt: "example {{/*Identity()*/}}"
```

If an activity were started up which references this file as `workload=test.yaml`, then all
the activity params recognized by the stdout driver would be valid, in addition to the core
activity params documented in this section.

*examples*

- `driver=stdout` - set the default driver to `stdout`

## workload

- _default_: unset, _required_: one of `workload=` or `op=` or `stmt=`, _dynamic_: no

- `workload=<filename>` where filename is a
  [YAML](https://yaml.org/),
  [JSON](https://json.org/), or
  [Jsonnet](https://jsonnet.org/) file with matching extension.
  If the extension is missing, then it is presumed to be a yaml file. Workload filenames are
  resolved on the local filesystem first, then from the files which are bundled into the
  nosqlbench binary or jar.
- `workload="<URL>"` where [URL](https://en.wikipedia.org/wiki/URL) with an valid
  [scheme](https://developer.mozilla.org/en-US/docs/Learn/Common_questions/Web_mechanics/What_is_a_URL#scheme),
  like http, https, or
  [S3](https://docs.aws.amazon.com/AmazonS3/latest/userguide/access-bucket-intro.html#accessing-a-bucket-using-S3-format).
  S3 support has been added directly, so you can use these URIs so long as you have a valid AWS
  configuration.
- `workload="<JSON Object>"` where the param value is a JSON object starting with `{`. Escaping
  might be necessary for some characters when using this on the command line.
    - example: `workload='{"bindings":{"b1":"NumberNameToString()"},"op":"testing {b1}"}'`

The workload param tells an activity where to load its [workload template](../../tutorials/workload-basics/_index/) from. The workload template is a collection of op templates which
are blueprints for the operations that an activity runs.

If the file is a Jsonnet file (by extension), then a local jsonnet interpreter will be run
against it before being handled as above. Within this evaluation context, all provided activity
parameters are available as external variables and accessible via the standard Jsonnet APIs,
specifically [std.extVar(str)](https://jsonnet.org/ref/stdlib.html#extVar). For doing robust data
type conversion, use [std.parseJson(str)](https://jsonnet.org/ref/stdlib.html#parseJson) by default.

## op

This is a serialized version of an operation to be parsed as an op template. It can be in one of
a few supported forms: JSON, as indicated by a leading `{` character, or a simple params map,
which is indicated by interior `name=value` assignments. If you want to provide a simpler form
which is representative a string statement, use the stmt form below.

## stmt

This is a short form version of an op template which contains a single op field for `stmt`,
which is the default form for any _statement-oriented_ operations in common protocols like CQL,
SQL, stdout, or similar. If you need to provide more op template details than this form allows,
then either use the _op_ form above, or provide a full workload.

## tags

- `tags=<filterspec>`
- _default_: unset
- _required_: no
- _dynamic_ : no

Tags are used to filter the set of op templates presented for sequencing in an activity. Each op
template has a set of tags, which include two auto-tags that are provided by the runtime:

- `block` - the block name that contains the op template. All op templates are part of some
  block, even if they are configured at the root of a document. There is a virtual block named
  `block0` which all of the root-level op templates are assigned to.
- `name` - a unique name for the op template within the workload template. This is a
  concatenation of the block name, two dashes (--) and the base op template name. For example,
  an op in `block0` with a base name of `opexample2` would be `block0--opexample2`. This allows for
  regex matching that can be globally distinct within a workload.

The rules for tag filtering are explained in depth in the [Op Tags](../../tutorials/workload-basics/05-op-tags/)
of the [Workload Basics](../../tutorials/workload-basics/_index/) tutorial.

## threads

- `threads=<threads>`
- _default_: 1
- _required_: no
- _dynamic_: yes

You should set the _threads_ parameter when you need to ramp up a workload.

This controls how many threads will be started up for an activity to run its cycles with.

_default value_ : For now, the default is simply *1*. Users must be aware of this setting and
adjust it to a reasonable value for their workloads.

`threads=auto` : Set the number of threads to 10x the number of cores in your system. There is
no distinction here between full cores and hardware threads. This is generally a reasonable
number of threads to tap into the processing power of a client system.

`threads=__x` : When you set `threads=5x` or `threads=10x`, you will
Set the number of threads to some multiplier of the logical CPUs in the local system.

A good rule of thumb for setting threads for maximum effect is to set it relatively high, such
as 10XvCPU when running synchronous workloads (when not providing the async parameter), and to
5XvCPU for all async workloads. Variation in system dynamics make it difficult to peg an ideal
number, so experimentation is encouraged while you dial in your settings initially.

*examples*

- `threads=30x` - set the number of threads in an activity to 30 × cores.
- `threads=auto` - set the number of threads in an activity to 10 × cores.
- `threads=10` - set the number of threads in an activity to 10.

## cycles

- `cycles=<cycle count>`
- `cycles=<cycle min>..<cycle max>`
- _default_: 1
- _required_: no
- _dynamic_: no

The cycles parameter determines the starting and ending point for an activity. It determines
the range of values which will act as seed values for each operation. For each cycle of the
activity, a statement is built from a statement template and executed as an operation.

For each cycle in an activity, the cycle is used as the input to the binding functions. This
allows you to create numerical relationships between all of the data used in your activity.

If you do not set the cycles parameter, then it will automatically be set to the size of the
sequence. The sequence is simply the length of the op sequence that is constructed from the
active op templates and ratios in your activity.

You *should* set the cycles for every activity except for schema-like activities, or activities
which you run just as a sanity check of active statements.

In the `cycles=<cycle count>` version, the count indicates the total number of cycles, and is
equivalent to `cycles=0..<cycle max>`. In both cases, the max value is not the actual number
of the last cycle. This is because all cycle parameters define a closed-open interval. In other
words, the minimum value is either zero by default or the specified minimum value, but the
maximum value is the first value *not* included in the interval. This means that you can easily
stack intervals over subsequent runs while knowing that you will cover all logical cycles
without gaps or duplicates. For example, given `cycles=1000` and then `cycles=1000..2000`, and
then `cycles=2000..5K`, you know that all cycles between 0 (inclusive) and 5000 (exclusive)
have been specified.

*examples*

- `cycles=500` - run an activity over cycles [0,500), including 0 and 499, but not 500.
- `cycles=20M` - run an activity over cycles [0,20000000).
- `cycles=2k..3k` - run an activity over cycles [2000,3000).

## recycles

- `recycles=<recycle count>`
- `recycles=<recycle min>..<recycle max>`
- _default_: 1
- _required_: no
- _dynamic_: no

This is another layer of iteration around the cycles values. With this, it is easy to set any
activity to repeat arbitrarily, or to have multiple specific iterations of some base workload.
Recycles is effectively the higher-order ordinal which wraps repeated
use of the same cycles interval. The combination of cycle and recycle is mathematically
consistent. If you set `recycles=1 cycles=1`, then you will have one total cycle executed. If
you set `recycles=10 cycles=10`, then you will have 100. If either of them is effectively set
to zero, then no cycles will occur.

They are also both interval-specific, so canonically, `recycles=37..39 cycles=7..11` is distinct
from `recycles=39..41 cycles=7..11`, although this is of limited utility until recycles is hoisted
further into op execution. In the future, this value may be used, for example, to bracket
instancing of metrics around specific recycle values, so that metrics are collected distinctly
for each _recycle_.

## errors

- `errors=<error handler spec>`
- _default_: `errors=stop`
- _required_: no
- _dynamic_: no

This activity param allows you to specify what happens when an exception is thrown during
execution of an operation (within a cycle). You can configure any named exception to be handled
with any of the available handler verbs in the order your choosing.

👉 By default, any single error in any operation will cause your test to stop. This is not
generally what you want to do for significant test scenarios.

You generally want to configure this so that you can run an activity as long as needed without a
single error stopping the whole thing. However, it is important for users to know exactly how
this is configured, so it is up to the user to set this appropriately.

The detailed configuration of error handlers is covered in
[error handlers](../../guides/workload-design/error-handlers/)

## maxtries

- `maxtries=<maxtries>`
- _default_: `maxtries=10`
- _required_: no
- _dynamic_: no

This sets the number of times an operation will be retried in the event that it fails and the
error handler is set to retry it.

## labels

- `labels=<label_key>:<label_value>[,...]`
- _default_: ``
- _required_: no
- _dynamic_: no

The labels provided in this form will be appended to the labels for this activity, used in
metrics reporting and annotations.

# Diagnostic

These params allow you to see more closely how an activity works for the purpose of
troubleshooting or test verification.

## dryrun

- `dryrun=<stepname>`
- _default_: unset
- _required_: no
- _dynamic_: no

This option is checked at various stages of activity initialization in order to modify the
way an activity runs. Some of the dryrun options stop an activity and dump out a summary of some
specific step. Others wrap normal mechanisms in a
[noop](https://en.wikipedia.org/wiki/NOP_(code)) in order to exercise other parts of the
machinery at full speed.

*examples*

- `dryrun=jsonnet` - When rendering a jsonnet workload, dump the result to the console and exit.
- `dryrun=op` - Wrap the operation in a noop, to measure core nb5 execution speed without
  invoking operations.

# Metrics

## alias

- `alias=<alias>`
- _default_: inferred from yaml, or 'UNSET'
- _required_: no
- _dynamic_: no

You *should* set the _alias_ parameter when you have multiple activities, when you want to name
metrics per-activity, or when you want to control activities via scripting.

Each activity can be given a symbolic name known as an _alias_. It is good practice to give all
your activities an alias, since this determines the named used in logging, metrics, and even
scripting control.

_default value_ : The name of any provided YAML filename is used as the basis for the default
alias. Otherwise, the activity type name is used. This is a convenience for simple test
scenarios only.

## instrument

- `instrument=<boolean>`
- _default_: false
- _required: no
- _dynamic_: no

This activity param allows you to set the default value for the instrument op field.

## hdr_digits

- `hdr_digits=<num digits>`
- _default_: `4`
- _required_: no
- _dynamic_: no

This parameter determines the number of significant digits used in all HDR
histograms for metrics collected from this activity. The default of 4
allows 4 significant digits, which means *up to* 10000 distinct histogram
buckets per named metric, per histogram interval. This does not mean that
there _will be_ 10000 distinct buckets, but it means there could be if
there is significant volume and variety in the measurements.

If you are running a scenario that creates many activities, then you can
set `hdr_digits=1` on some of them to save client resources.

# Customization

## cyclerate

- `cyclerate=<cycle per second>`
- `cyclerate=<cycles per second>,<burst_ratio>`
- _default_: unset
- _required_: no
- _dynamic_: yes

The cyclerate parameter sets a maximum op rate for individual cycles within the activity,
across the whole activity, irrespective of how many threads are active.

👉 The cyclerate is a rate limiter, and can thus only throttle an activity to be slower than it
would otherwise run. Rate limiting is also an invasive element in a workload, and will always
come at a cost. For extremely high throughput testing, consider carefully whether your testing
would benefit more from concurrency-based throttling such as adjust the number of threads.

When the cyclerate parameter is provided, two additional metrics are tracked: the wait time and
the response time. See timing terminology for more details.

When you try to set very high cyclerate values on systems with many cores, the performance will
degrade. Be sure to use dryrun features to test this if you think it is a limitation. You can
always set the rate high enough that the rate limiter can't sustain. This is like telling it to
get in the way and then get out of the way even faster. This is just the nature of this type of
rate limiter.

There are plans to make the rate limiter adaptive across a wider variety of performance
scenarios, which will improve this.

*examples*

- `cyclerate=1000` - set the cycle rate limiter to 1000 ops/s and a
  default burst ratio of 1.1.
- `cyclerate=1000,1.0` - same as above, but with burstrate set to 1.0
  (use it or lose it, not usually desired)
- `cyclerate=1000,1.5` - same as above, with burst rate set to 1.5 (aka
  50% burst allowed)

**burst ratio**

This is only an optional part of the cyclerate as shown in examples above.
If you do not specify it when you initialize a cyclerate, then it defaults
1.1. The burst ratio is only valid as part of a rate limit and can not be
specified by itself.

* _default_: `1.1`
* _dynamic_: yes

The NoSQLBench rate limiter provides a sliding scale between strict rate
limiting and average rate limiting. The difference between them is
controlled by a _burst ratio_ parameter. When the burst ratio is 1.0
(burst up to 100% relative rate), the rate limiter acts as a strict rate
limiter, disallowing faster operations from using time that was previously
forfeited by prior slower operations. This is a "use it or lose it" mode
that means things like GC events can steal throughput from a running
client as a necessary effect of losing time in a strict timing sense.

When the burst ratio is set to higher than 1.0, faster operations may
recover lost time from previously slower operations. For example, a burst
ratio of 1.3 means that the rate limiter will allow bursting up to 130% of
the base rate, but only until the average rate is back to 100% relative
speed. This means that any valleys created in the actual op rate of the
client can be converted into plateaus of throughput above the strict rate,
but only at a speed that fits within (op rate * burst ratio). This allows
for workloads to approximate the average target rate over time, with
controllable bursting rates. This ability allows for near-strict behavior
while allowing clients to still track truer to rate limit expectations, so
long as the overall workload is not saturating resources.

👉 The default burst ratio of 1.1 makes testing results slightly more stable
on average, but can also hide some short-term slow-downs in system
throughput. It is set at the default to fit most tester's expectations for
averaging results, but it may not be strict enough for your testing
purposes. However, a strict setting of 1.0 nearly always adds cold/startup
time to the result, so if you are testing for steady state, be sure to
account for this across test runs.

## striderate

- `striderate=<strides per second>`
- `striderate=<strides per second>,<burst_ratio>`
- _default_: unset
- _required_: no
- _dynamic_: yes

The `striderate` parameter allows you to limit the start of a stride
according to some rate. This works almost exactly like the cyclerate
parameter, except that it blocks a whole group of operations from starting
instead of a single operation. The striderate can use a burst ratio just
as the cyclerate.

This sets the target rate for strides. In NoSQLBench, a stride is a group
of operations that are dispatched and executed together within the same
thread. This is useful, for example, to emulate application behaviors in
which some outside request translates to multiple internal requests. It is
also a way to optimize a client runtime for more efficiency and
throughput. The stride rate limiter applies to the whole activity
irrespective of how many threads it has.

**WARNING:**
When using the cyclerate and striderate options together, operations are
delayed based on both rate limiters. If the relative rates are not
synchronised with the size of a stride, then one rate limiter will
artificially throttle the other. Thus, it usually doesn't make sense to
use both of these settings in the same activity.

## stride

- `stride=<stride>`
- _default_: same as op sequence length
- _required_: no
- _dynamic_: no

Usually, you don't want to provide a setting for stride, but it is still important to
understand what it does. Within NoSQLBench, each time a thread needs to allocate a set of
cycles to run, it takes a contiguous range of values from an activity-wide source, usually an
atomic sequence. Thus, the stride is the unit of micro-batching within NoSQLBench. It also
means that you can use stride to optimize a workload by setting the value higher than the
default. For example if you are running a single-statement workload at a very high rate, it
doesn't make sense for threads to allocate one op at a time from a shared atomic value. You can
simply set `stride=1000` to cause (ballpark estimation) about 1000X less internal contention.
The stride is initialized to the calculated sequence length. The sequence length is simply the
number of operations in the op sequence that is planned from your active statements and their
ratios.

You usually do not want to set the stride directly. If you do, make sure it is a multiple of
what it would normally be set to if you need to ensure that sequences are not divided up
differently. This can be important when simulating the access patterns of applications.

*examples*

- `stride=1000` - set the stride to 1000

## seq

- `seq=<bucket|concat|interval>`
- _default_: `seq=bucket`
- _required_: no
- _dynamic_: no

The `seq=<bucket|concat|interval>` parameter determines the type of
sequencing that will be used to plan the op sequence. The op sequence is a
look-up-table that is used for each stride to pick statement forms
according to the cycle offset. It is simply the sequence of statements
from your YAML that will be executed, but in a pre-planned, and highly
efficient form.

An op sequence is planned for every activity. With the default ratio on
every statement as 1, and the default bucket scheme, the basic result is
that each active statement will occur once in the order specified. Once
you start adding ratios to statements, the most obvious thing that you
might expect will happen: those statements will occur multiple times to
meet their ratio in the op mix. You can customize the op mix further by
changing the seq parameter to concat or interval.

👉 The op sequence is a look-up table of op templates, *not*
individual statements or operations. Thus, the cycle still determines the
uniqueness of an operation as you would expect. For example, if statement
form ABC occurs 3x per sequence because you set its ratio to 3, then each
of these would manifest as a distinct operation with fields determined by
distinct cycle values.

There are three schemes to pick from:

**bucket**

This is a round-robin planner which draws operations from buckets in
circular fashion, removing each bucket as it is exhausted. For example,
the ratios A:4, B:2, C:1 would yield the sequence A B C A B A A. The
ratios A:1, B5 would yield the sequence A B B B B B.

**concat**

This simply takes each statement template as it occurs in order and
duplicates it in place to achieve the ratio. The ratios above (A:4, B:2,
C:1) would yield the sequence A A A A B B C for the concat sequencer.

**interval**

This is arguably the most complex sequencer. It takes each ratio as a
frequency over a unit interval of time, and apportions the associated
operation to occur evenly over that time. When two operations would be
assigned the same time, then the order of appearance establishes
precedence. In other words, statements appearing first win ties for the
same time slot. The ratios A:4 B:2 C:1 would yield the sequence A B C A A
B A. This occurs because, over the unit interval (0.0,1.0), A is assigned
the positions `A: 0.0, 0.25, 0.5, 0.75`, B is assigned the
positions `B: 0.0, 0.5`, and C is assigned position `C: 0.0`. These
offsets are all sorted with a position-stable sort, and then the
associated ops are taken as the order.

In detail, the rendering appears
as `0.0(A), 0.0(B), 0.0(C), 0.25(A), 0.5(A), 0.5(B), 0.75(A)`, which
yields `A B C A A B A` as the op sequence.

This sequencer is most useful when you want a stable ordering of operation
from a rich mix of statement types, where each operation is spaced as
evenly as possible over time, and where it is not important to control the
cycle-by-cycle sequencing of statements.
