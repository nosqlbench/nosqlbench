---
title: Binding Concepts
weight: 10
---

NoSQLBench has a built-in library for the flexible management and expressive use of
procedural generation libraries. This section explains the core concepts
of this library, known as _Virtual Data Set_.

## Variates (Samples)

A numeric sample that is drawn from a distribution for the purpose
of simulation or analysis is called a *Variate*.

## Procedural Generation

Procedural generation is a category of algorithms and techniques which take
a set or stream of inputs and produce an output in a different form or structure.
While it may appear that procedural generation actually _generates_ data, no output
can come from a void. These techniques simply perturb a value in some stateful way,
or map a coordinate system to another representation. Sometimes, both techniques are
combined together.

## Uniform Variate

A variate (sample) drawn from a uniform (flat) distribution is what we are used
to seeing when we ask a system for a "random" value. These are often produced in
one of two very common forms, either a register full of bits as with most hashing
functions, or a floating point value between 0.0 and 1.0. (This is called the _unit
interval_).

Uniform variates are not really random. Without careful attention to API usage,
such random samples are not even unique from session to session. In many systems,
the programmer has to be very careful to seed the random generator or they will
get the same sequence of numbers every time they run their program. This turns out
to be a useful property, and the random number generators that behave this way are
usually called Pseudo-Random Number Generators, or PRNGs.

## Apparently Random Variates

Uniform variates produced by PRNGs are not actually random, even though they may
pass certain tests for randomness. The streams of values produced are nearly
always measurably random by some meaningful standard. However, they can be
used again in exactly the same way with the same initial seed.

## Deterministic Variates

If you intentionally avoid randomizing the initial seed for a PRNG, for example,
with the current timestamp, then it gives you a way to replay a sequence.
You can think of each initial seed as a _bank_ of values which you can go back
to at any time. However, when using stateful PRNGs as a way to provide these
variates, your results will be order dependent.

## Randomly Accessible Determinism

Instead of using a PRNG, it is possible to use a hash function instead. With a 64-bit
register, you have 2^64 (2^63 in practice due to available implementations) possible
values. If your hash function has high dispersion, then you will effectively
get the same result of apparent randomness as well as deterministic sequences, even
when you use simple sequences of inputs to your _random()_ function. This allows
you to access a random value in bucket 57, for example, and go back to it at any
time and in any order to get the same value again.

## Data Mapping Functions

The data mapping functions are the core building block of virtual data set.
Data mapping functions are generally pure functions. This simply means that
a generator function will always provide the same result given the same input.
The parameters that you will see on some binding recipes are not representative
of volatile state. These parameters are initializer values which are part of a
function's definition. For example a `Mod(5)` will always behave like a `Mod(5)`,
as a pure function. But a `Mod(7)` will be have differently than a `Mod(5)`, although
each function will always produce its own stable result for a given input.

## Combining RNGs and Data Mapping Functions

Because pure functions play such a key part in procedural generation techniques,
the terms "data mapping function", "data mapper" and "data mapping library" will
be more common in the library than "generator". Conceptually, mapping functions
to not generate anything. It makes more sense to think of mapping data from one
domain to another. Even so, the data that is yielded by mapping functions can
appear quite realistic.

Because good RNGs do generally contain internal state, they aren't purely
functional. This means that in some cases -- those in which you need to have
random access to a virtual data set, hash functions make more sense. This
toolkit allows you to choose between the two in some cases. However, it
generally favors using hashing and pure-function approaches where possible. Even
the statistical curve simulations do this.

## Bindings Template

It is often useful to have a template that describes a set of generator
functions that can be reused across many threads or other application scopes. A
bindings template is a way to capture the requested generator functions for
re-use, with actual scope instantiation of the generator functions controlled by
the usage point. For example, in a JEE app, you may have a bindings template in
the application scope, and a set of actual bindings within each request (thread
scope).

