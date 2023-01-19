---
weight: 225811593
title: virdata
---
# virdata

NB5 contains a bundled virtdata app which lets you verify bindings from the command line.
It is useful for sanity checking values as well as getting a concurrent performance baseline for
specific binding recipes.

# diagnostic mode

The `diagnose` mode of virtdata can be used to explain binding resolution logic and potentially
how to fix a binding that doesn't resolve properly:

```shell
nb5 virtdata diagnose "Combinations('0-90-9'); ToInt();"
```

# perftest mode

To see the options you can simply run `nb5 virtdata testmapper`, which gives you

```
ARGS: checkperf 'specifier' threads bufsize start end
example: 'timeuuid()' 100 1000 0 10000
specifier: A VirtData function specifier.
threads: The number of concurrent threads to run.
bufsize: The number of cycles to give each thread at a time.
start: The start cycle for the test, inclusive.
end: The end cycle for the test, exclusive.
```

Assuming you have a working binding recipe that you want to measure for
concurrent performance, you can use the form below:

```shell
nb5 virtdata testmapper "Combinations('0-90-9'); ToInt();" 96 1000 0 1000000
```

Here is what happens when you run this:

1. The binding is resolved.
2. A single thread (the first thread) generates the values into a reference buffer.
3. The specified number of threads is started, and synchronized for simultaneous start.
4. Each thread is given successive batches of input values from the cycle rage specified, in
   chunks of bufsize each. A dot `.` is printed to the console for each completed chunk.
5. Once all threads are complete, each checks their values against the reference buffer, and an
   exception is thrown if any difference are found. (This would mean concurrency is affecting
   the values, which is not allowed for binding functions.)
6. After all chunks are generated and verified, statistics are displayed to the console:

## output details

```text,linenos
         run data = [derived values in brackets]
        specifier = 'Combinations('0-90-9'); ToInt();'
          threads = 96
              min = 0
              max = 1000000
          [count] = 1000000
       buffersize = 1000
 [totalGenTimeMs] = 2408.874399
 [totalCmpTimeMs] = 2274.510746
       [genPerMs] = 39852.638
       [cmpPerMs] = 42206.879
        [genPerS] = 39852638.245
        [cmpPerS] = 42206879.070
```

This shows that on a 12 core (24 thread) system, Around **40 million** variates are able to be
generated from the above recipe (across all cores, of course).

In detail:
* `[totalGenTimeMs]` tracks the total time the thread pool spent generating data, in milliseconds.
* `[totalCmpTimeMs]` tracks the total time the thread pool spent cross-checking data across threads.
* `[genPerMs]` and `[cmpPerMs]` show the calculated rates for generation and validation _per
  millisecond_, respectively.
* `[genPerS]` and `[cmpPerS]` show the calculated rates for generation and validation _per second_,
  respectively.

## interpretation

This example shows how effective variate generation can be. This doesn't mean that you can
easily simulate 40 million operations with this data. However, it does _anecdotally_ indicate
the proportional load that generation puts on the system. For example, if you were generating
around 400K ops/s from a client with only this binding, it would be reasonable that variate
generation consumes around (400,000/40,000,000) of the system's cycles, or around 1%.

More realistic testing scenarios are likely to use proportionally more due to the amount of data
generation which is needed. Still, generating synthetic data makes for a more capable testing
harnesses because of the extra headroom you leave in your system for other necessary work, like
managing a driver's connection pool or serdes on requests and responses.

