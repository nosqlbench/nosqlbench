# Intro

As part of the pulsar driver, we're making an effort to introduce a way to
scope the construction of an executable operation that can be hoisted up
to the API level and used across driver going forward.

## The Gist

When building pulsar operations in a normal client, the association
between the messages, the topic they are broadcast on, the producer that
they are sent from, and the client -- All these things are up to the
designer and may vary from application to application. For example, in
some applications, topics may be fine-grained, and subdivided using thread
associativity. In others, many producers may write to the same topic
across many threads. The ability to test both the client and server
infrastructure in ways that are meaningful and relatable to real
applications depends on having some flexible wiring within nosqlbench.

In a more mechanical sense, this means:

1) Controlling how clients are instantiated in nosqlbench.
2) For each client, controlling how producers or consumers are
   instantiated.
3) For each producer or consumer, controlling how topics are selected.

There is an imposed order to the construction pattern of a pulsar op in
the client API: You must follow the product chain from the API to the call
which constitutes an operation:

```
Pulsar API -> Client* -> Producer(topic) -> send(...)
Pulsar API -> Client* -> Consumer(topic) -> receive(...)
```

As well, additional intermediate steps in the builder APIs may be needed
to qualify schema, etc.

## Valid Constructions

The instantiation pattern imposed by the Pulsar API combined with the
execution model imposed by nosqlbench overlap. Within the nb model, it is
conceivable that a user would want to be able to script the whole
construction of an operation in a scenario, outside the scope of an
activity. It is also conceivable that they would want to create the object
stack that backs an operation local to the cycle, for highly variant
testing. These two extremes represent the range of possibilities that
should ideal be supported.

Here, the patterns of the Pulsar API and the nb APIs are combined into one
view:

```text

            process  scenario activity  thread   cycle
 client     OK       OK       OK        OK       OK
 topic      OK       OK       OK        OK       OK
 producer   OK       OK       OK        OK       OK
 operation                                       OK

```

This illustrates where each part of the client object stack may be created
within a valid nosqlbench runtime. The cycle is required to produce the
op-specific data, which may govern the construction of the higher layers.
As well, there is a dependency from the operation up to the client. This
means there are a couple key rules for valid constructions which may seem
obvious but which warrant stating clearly:

1) The Pulsar client stack must be instantiated in dependency order.
2) The operation may only be fully realized within a cycle.

## Caching

If we're only looking at how to make a correctly instantiated operation
with the right dependencies, then the implementation is much easier. The
reason for laying out the concerns above in more detail are to enable a
runtime which is efficient enough as a test instrument to be worthy of
serious testing. For example, we could simply construct the whole object
stack per cycle according to the properties of the op template. This would
be easy to implement, but it would be so egregiously inefficient to the
point of being useless.

On the contrary, the goal is to construct each layer as needed in order to
retain in memory a set of ready objects which can be shared across the
broadest scope while meeting the tester's requirements for logical scoping
and affinity between messages and other runtime scaffolding that would be
found in a typical application or message processor.

## TL State, Anonymous State

In order to build possibly the whole stack for a given cycle (to support
multi-tenant messaging patterns, for example), the construction logic must
be available within the cycle. In order to do the same programmatically,
as if from a scenario script, the construction logic must be callable
anonymously, with the op being instantiated with the help of a cycle
value.

## De-cluttering

These types of construction patterns have a serious history of being
cluttered and over-complicated in practice. It is a goal of this design to
avoid this baggage and come up with something that is understandable,
efficient, and re-usable across activity type implementations. Thus, if it
must be demonstrated as an explicit code pattern and not pushed into a
library or type system for imposing DRY, so be it. This is an acceptable
trade-off in order to avoid an opaque and unapproachable example. Clearly,
having a code pattern that is testable and explainable is immensely more
valuable than one that isn't, assuming equivalent behavior.

# Initial Design

TBD: Extract design into this doc for discussion

1. start with Command Template
2. extract naming functions for each layer
3. Call into caching layer with proper closures to get named instances
4. Encourage a pattern of exhaustive parameter consumption for initial
   sanity checking.
5. Add defensive warnings and checks for valid consstructions



