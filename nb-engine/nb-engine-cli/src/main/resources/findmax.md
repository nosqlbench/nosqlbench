Analysis Method: FindMax
========================

The findmax.js script can be used with any normal activity definition
which uses the standard phase tagging scheme. It searches for the maximum
throughput available which satisfy a basic SLA requirement. It does this
by dynamically adjusting the workload while it runs, iteratively adjusting
the performance goals against known results. In short, it does what we
often do when we're searching for a local maxima in performance
parameters.

This particular analysis method is not open-ended in terms of its
parameters. That is, it is designed (for now) to focus only on a set of
known parameters: Namely the target throughput as the independent variable
and the resulting response time as the dependent variable.

Here is how the algorithm works.

1. The baseline is set to zero, and the stepping size is set to a small
   value.
2. The stepping size is added to the baseline to determine the current
   target rate.
3. The workload is run for a period of time (10 seconds by default) and
   the achieved throughput and service times are measured.
4. Boundary conditions are checked to determine whether the settings for
   this sampling window are acceptable: These conditions are:
    1. The achieved throughput is high enough with respect to the target
       rate (min 80% by default).
    2. The achieved throughput is not lower than the best known result.
    3. The achieved latency is low enough (p99<50ms).
5. If any of the boundary conditions are not met, then target throughput
   for the current iteration is deemed unusable and the search parameters
   are adjusted. In this case, the baseline is check-pointed to that of
   the highest valid result, and the step increases start again at the
   base step size.
6. If the conditions are met, then the incremental target rate is scaled
   up by a factor. The incremental target rate is simply the base rate
   plus the step size multiplied by some factor.
7. If increasing the target by the minimum step size would set a target
   rate that is known to be too high from a previous run, then the test
   terminates.

The findmax script actually runs multiple times, and takes the average
result.

### Example Output (start of run)

      # This shows findmax just starting up, where the target rates are
      # still pretty low.

      >-- iteration 1 ---> targeting 100 ops_s (0+100) for 10s
      LATENCY         PASS(OK)       [ 3.70ms p99 < max 50ms ]
      OPRATE/TARGET   PASS(OK)       [ 102% of target > min 80% ] ( 102/100 )
      OPRATE/BEST     PASS(OK)       [ 100% of best known > min 90% ] ( 102/102 )
      ---> accepting iteration 1

      >-- iteration 2 ---> targeting 200 ops_s (0+200) for 10s
      LATENCY         PASS(OK)       [ 3.62ms p99 < max 50ms ]
      OPRATE/TARGET   PASS(OK)       [ 100% of target > min 80% ] ( 199/200 )
      OPRATE/BEST     PASS(OK)       [ 195% of best known > min 90% ] ( 199/102 )
      ---> accepting iteration 2

      >-- iteration 3 ---> targeting 400 ops_s (0+400) for 10s
      LATENCY         PASS(OK)       [ 3.57ms p99 < max 50ms ]
      OPRATE/TARGET   PASS(OK)       [ 100% of target > min 80% ] ( 399/400 )
      OPRATE/BEST     PASS(OK)       [ 201% of best known > min 90% ] ( 399/199 )
      ---> accepting iteration 3

      >-- iteration 4 ---> targeting 800 ops_s (0+800) for 10s
      LATENCY         PASS(OK)       [ 3.70ms p99 < max 50ms ]
      OPRATE/TARGET   PASS(OK)       [ 99% of target > min 80% ] ( 796/800 )
      OPRATE/BEST     PASS(OK)       [ 199% of best known > min 90% ] ( 796/399 )
      ---> accepting iteration 4

      >-- iteration 5 ---> targeting 1600 ops_s (0+1600) for 10s
      LATENCY         PASS(OK)       [ 3.53ms p99 < max 50ms ]
      OPRATE/TARGET   PASS(OK)       [ 99% of target > min 80% ] ( 1591/1600 )
      OPRATE/BEST     PASS(OK)       [ 200% of best known > min 90% ] ( 1591/796 )
      ---> accepting iteration 5

      ...

### Example Result (end of run)

### Parameters

#### sampling controls ####

- `sample_time=10` - How long each sampling window lasts in seconds,
  initially.
- `sample_incr=1.33` - How to scale up sample_time each time the search
  range is adjusted.
- `sample_max=300` - The maximum sampling window size in seconds.

The sample time determines how long each iteration lasts. As findmax runs,
this value is increased according to the sample_incr value. This improves
accuracy as test results start to converge, but it also makes the test
take longer. Lower values of sample_incr or sample_max will limit the
runtime of tests, but this will also limit accuracy.

#### rate controls ####

- `rate_base=0` - The initial base rate.
- `rate_step=100` - The minimum step size to be added to the base rate.
- `rate_incr=2` - The increase factor for the step size.

By default, these parameters approximate the speed of binary search for
each time the base parameter is check-pointed. The target rate is
calculated as `rate_base + (rate_step * rate_incr ^ step)` where steps
start at 0. For example, the initial iteration will thus use `0 +
(100 * 2^0)` which is simply 100.) Based on this, the initial target rates
will follow a pattern of 100,200,400,800, and so on. Once the highest
successful target rate is found in this series, the base rate is
check-pointed to that, and the search starts again with that new base rate
with the same progression above stacking on top.

- `min_stride=100` - A lock-avoiding optimization for rate limiting. This
  uses a stride rate limiter with micro-batching internally to avoid
  higher contention on many-cored client systems.

- `averageof=2` - How many runs to average together for the final result.
  Averaging multiple runs together ensures that some noise of local
  effects (like background processes or compactions) are smoothed out for
  a more realistic result.

#### pass/fail conditions ####

- `latency_cutoff=50.0` - The numeric value which is considered the
  highest acceptable latency at the selected percentile.
- `latency_pctile=0.99` - The selected percentile. The values shown here
  for these two mean "50ms@p99".
- `testrate_cutoff=0.8` - The minimum achieved throughput for an iteration
  with respect to the target rate to be considered successful.
- `bestrate_cutoff=0.9` - The minimum achieved throughput for an iteration
  with respect to the known best result to be considered successful.

## Workload Selection

- `activity_type=cql` - The type of internal NB driver to use.
- `yaml_file=cql-iot` - The name of the workload yaml.

You can invoke findmax with any workload yaml which uses the standard
naming scheme for phase control. This means that you have tagged each of
your statements or statement blocks with the appropriate phase tags from
schema, rampup, main, for example.

- `schematags=block:"schema.*"` - The tag filter for schema statements.
  Findmax will run a schema phase with 1 thread by default.
- `maintags=block:main` - The tag filter for the main workload. This is
  the workload that is started and run in the background for all of the
  sampling windows.

