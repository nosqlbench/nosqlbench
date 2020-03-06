# Virtual Dataset Concepts

VirtData is a library for the flexible management and expressive use of
procedural generation libraries. It is a reincarnation of a previous project.
This version of the idea starts by focusing directly on usage aspects and
extension points rather than the big idea.

### Procedural Generation

Procedural generation is a general class of methods for taking a set of inputs
and modifying them in a predictable way to generate content which appears random
but is actually deterministic. For example, some games use procedural generation
to take a single value known as the "seed" to generate an apparently rich and
interesting world.

### Apparently Random RNGs

Sequences of values produced by RNGs (more properly called PRNGs) are not
actually random, even though they may pass certain tests for randomness. In
practice, the combination of these two properties is quite valuable for testing
and data synthesis. Having a stream of data that is measurably random by some
meaningful standard, but which is configurable and reusable allows for test to
be replayed, for example.

### Apparently Random Samples

Just as RNGs can appear random when the are not truly, statistical distributions
which rely on them can also appear random. Uniform random number generators over
the unit interval [0,1.0) are a common input to virtual sampling methods. This
means that if you can configure the RNG stream that you feed into your virtual
sampling methods, you can simulate a repeatable sequence from a known
distribution.

### Data Mapping Functions

The data mapping functions are the core building block of virtdata. They are the
functional logic that powers all procedural generation. Data mapping functions
are generally pure functions. This simply means that a generator function will
always provide the same result given the same input. All top-level mapping
functions all take a long value as their input, and produce a result based on
their parameterized type.

##### Combining RNGs and Data Mapping Functions

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

### Data Mapper Library

Data Mapping functions are packaged into libraries which can be loaded by the
virtdata-user component of the project. Each library has a name, a function
resolver, and a set of functions that can be instantiated via the function
resolver.

### Function Resolver

Each library must implement its own function resolver. This is because each
library may have a different way of naming, finding, creating or managing
function generator instances. For the user, the description of a generator is
simply a string. What the generator library does with it is
implementation-specific. This means that some generator libraries may simply
have constructor signatures as function specifiers, and others may go as far as
implementing their own DSL. The basic contract for a function resolver is that
you pass it a string describing what you want, and it provides a generator
function in return.

#### Bindings Template

It is often useful to have a template that describes a set of generator
functions that can be reused across many threads or other application scopes. A
bindings template is a way to capture the requested generator functions for
re-use, with actual scope instantiation of the generator functions controlled by
the usage point. For example, in a JEE app, you may have a bindings template in
the application scope, and a set of actual bindings within each request (thread
scope).

