---
title: Async Operations
weight: 35
menu:
  main:
    parent: Dev Guide
    identifier: Async Operations
    weight: 12
---

{{< warning >}}
This section is out of date, and will be updated after the next major release
with details on building async activity types.
{{< /warning >}}


## Introduction

In nosqlbench, two types of activities are supported: sync or async. Going forward, the async interface will be
refined and hardened, and then the sync interface will be deprecated. This is simply a matter of simplifying the
API over time, and the async interface is the essential one. If you want synchronous behavior with the async
interface, you can easily achieve that, but not the other way around.

### Configuring Async

In an async activity, you still have multiple threads, but in this case, each thread is allowed to juggle one or
more asynchronous operations. The `async=100` parameter, for example, informs an activity that it needs to allocate
100 total operations over the allocated threads. In the case of `async=100 threads=10`, it is the responsibility
of the ActivityType's action dispenser to configure their actions to know that each of them can juggle 10 operations
each. 

{{< note >}}The *async* parameter has a standard meaning in nosqlbench. If it is defined, async is enabled. Its
parameter value is the number of total async operations that can be in flight at any one instant, with the number
per-thread divided as evenly as possible over all the threads.

If the async parameter is defined, but the action implementation does *not* implement the async logic,
then an error is thrown to the user. This is to ensure that users are aware of when they are expecting async
behavior but getting something else.
{{</ note >}}

### Async Messaging Flow

The contract between a motor and an action is very basic.

- Each motor submits as many async operations as is allowed to its action, as long as there are 
  cycles remaining, until the action signals that it is at its limit.
- As long as an action is able to retire an operation by giving a result back to its motor,
  the motor keeps providing one more and retiring one more, as long as there are cycles remaining.
- Once there are no more cycles remaining, the motor retires operations from the action until
  the action signals that no more are pending.

The basic result of this is that each thread ramps up to its async juggling rate, hovers at that
rate, with a completion rate dependent on the target system, and then ramps down as pending ops
are retired back down to zero.

### Advanced Signaling

Because of differences in client-side APIs and behavior, and the need to do simple and reliable
flow management in nosqlbench, there are a few details about the API that are worth understanding
as a developer.

- There are multiple return or signaling points in the lifetime of an op context:
  1. When an action becomes aware that an operation has completed, it is up to the action to
     mark the op context with a call to `opcontext.stop(result)` at that time. This is important,
     because operations do not complete in the same order that they are submitted, especially
     when other async logic is present. A common way to do this is to register a callback on
     a listener, for example.
  2. The action must still return this op context back to the motor when it is asked. Thus, it
     is a common pattern to keep a linked list of operations that are ready to retire and thus
     allow the action to control orderly shutdown of the motor without any guesswork about the
     completion state of pending operations.
  3. The op context can be a sub-type, if you need to specialize the details that you keep for
     an in-flight operation. In fact, async actions *must* implement a basic factory method,
     but it can return a simple op context if no specialization is needed.
  4. op contexts are recycled to avoid heap pressure for high data rates. This makes it relatively
     low-cost to use the specialized op context to hold contextual data that may otherwise be
     expensive to _malloc_ and _free_. 
 
### Examples

Developers can refer to the Diag activity type implementation for further examples. 