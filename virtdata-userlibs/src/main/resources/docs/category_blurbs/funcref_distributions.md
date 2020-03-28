---
title: distribution functions
weight: 30
---

All of the distributions that are provided in the Apache Commons Math
project are supported here, in multiple forms.

## Continuous or Discrete

These distributions break down into two main categories:

### Continuous Distributions

These are distributions over real numbers like 23.4323, with
continuity across the values. Each of the continuous distributions can
provide samples that fall on an interval of the real number line.
Continuous probability distributions include the *Normal* distribution,
and the *Exponential* distribution, among many others.

### Discrete Distributions

Discrete distributions, also known as *integer distributions* have only
whole-number valued samples. These distributions include the *Binomial*
distribution, the *Zipf* distribution, and the *Poisson* distribution,
among others.

## Hashed or Mapped

### hashed samples

Generally, you will want to "randomly sample" from a probability distribution.
This is handled automatically by the functions below if you do not override the
defaults. **The `hash` mode is the default sampling mode for probability
distributions.** This is accomplished by computing an internal on the unit
interval variate input before using the resulting value to map into the sampling
curve. This is called the `hash` sampling mode by VirtData. You can put `hash`
into the modifiers as explained below if you want to document it explicitly.

### mapped samples

The method used to sample from these distributions depends on a mathematical
function called the cumulative probability function, or more specifically
the inverse of it. Having this function computed over some interval allows
one to sample the shape of a distribution progressively if desired. In
other words, it allows for some *percentile-like* view of values within
a given probability distribution. This mode of using the inverse cumulative
density function is known as the `map` mode in VirtData, as it allows one
to map a unit interval variate in a deterministic way to a density
sampling curve. To enable this mode, simply pass `map` as one of the
function modifiers for any function in this category.

## Interpolated or Computed Samples

When sampling from mathematical models of probability densities, performance
between different densities can vary drastically. This means that you may
end up perturbing the results of your test in an unexpected way simply
by changing parameters of your testing distributions. Even worse, some
densities have painful corner cases in performance, like 'Zipf', which
can make tests unbearably slow and flawed as they chew up CPU resources.

### Interpolated Samples

For this reason, interpolation is built-in to these sampling functions.
**The default mode is `interpolate`.** This means that the sampling
function is pre-computed over 1000 equidistant points in the unit interval,
and the result is shared among all threads as a look-up-table for
interpolation. This makes all statistical sampling functions perform nearly
identically at runtime (after initialization, a one time cost).
This does have the minor side effect of a little loss in accuracy, but
the difference is generally negligible for nearly all performance testing
cases.

### Computed Samples

Conversely, `compute` mode sampling calls the sampling function every
time a sample is needed. This affords a little more accuracy, but is generally
not preferable to the default interpolated mode. You'll know if you need
computed samples. Otherwise, it's best to stick with interpolation so that
you spend more time testing your target system and less time testing
your data generation functions.

## Input Range

All of these functions take a long as the input value for sampling. This
is similar to how the unit interval (0.0,1.0) is used in mathematics
and statistics, but more tailored to modern system capabilities. Instead
of using the unit interval, we simply use the interval of all positive
longs. This provides more compatibility with other functions in VirtData,
including hashing functions.


